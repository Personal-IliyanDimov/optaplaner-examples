# Order distribution among experts
_Exported on 4/7/2026 at 12:22:21 GMT+3 from Cursor (2.6.20)_

---

**User**

I see that for the following day:
===
========== 2026-03-04 (WEDNESDAY) ==========
--- Expert: Alice ---
Expert skills: Electrical
Expert availability: 2026 | CW10 wd=3 | 10:30 - 14:00
Expert absence: (none for this day)
Expert scheduled items:
  - customer: Customer-6 | due: 2026-03-05 | start: 11:45 | duration: 2h | location: (18.01757, 7.56354) | order skills: Electrical

--- Expert: Bob ---
Expert skills: Engine-Diesel, Engine-Petrol, Exclusive
Expert availability: 2026 | CW10 wd=3 | 09:00 - 18:00
Expert absence: (none for this day)
Expert scheduled items:
  - customer: Customer-5 | due: 2026-03-04 | start: 10:15 | duration: 1h 30m | location: (18.83800, 14.26448) | order skills: Exclusive
  - customer: Customer-4 | due: 2026-03-05 | start: 11:45 | duration: 2h | location: (17.60516, 11.44553) | order skills: Engine-Diesel, Engine-Petrol
  - customer: Customer-3 | due: 2026-03-05 | start: 14:00 | duration: 30m | location: (6.38492, 14.43255) | order skills: Engine-Diesel, Exclusive
  - customer: Customer-2 | due: 2026-03-04 | start: 16:30 | duration: 30m | location: (17.86078, 9.46541) | order skills: Engine-Petrol

--- Expert: Carol ---
Expert skills: Electrical, Engine-Diesel, Exclusive, Painting-Scratches
Expert availability: 2026 | CW10 wd=3 | 09:00 - 18:00
Expert absence: (none for this day)
Expert scheduled items:
  - customer: Customer-1 | due: 2026-03-05 | start: 10:00 | duration: 1h 30m | location: (16.25073, 9.43172) | order skills: Engine-Diesel, Exclusive
===

Orders are not fairly distributed. Carol could take some of the Bob orders
  - customer: Customer-5 | due: 2026-03-04 | start: 10:15 | duration: 1h 30m | location: (18.83800, 14.26448) | order skills: Exclusive
  - customer: Customer-3 | due: 2026-03-05 | start: 14:00 | duration: 30m | location: (6.38492, 14.43255) | order skills: Engine-Diesel, Exclusive

This way the load will be distributed more evenly

---

**Cursor**

Exploring how scheduling constraints are defined so we can improve expert load distribution.



Identifying the issue: fairness uses total workload per expert across the whole horizon, so a balanced global score can still leave one day skewed (Bob 4 vs Carol 1). Adding per-calendar-day load balancing alongside the existing global fairness.



Here is what was going on and what we changed.

## Cause

`fairlyDistributePerExpertScheduledItems` already balances **total diagnosis minutes per expert over the whole planning horizon**. It does **not** look at each calendar day. So you can get a decent global score while **one day** is very uneven (Bob 4 jobs vs Carol 1), especially when other goals (due dates, travel, overlaps, customer windows) pull many jobs onto Bob.

## Change

A second **medium** constraint balances **per expert per calendar day** (same `FairnessDetector` metric: \(\sqrt{\sum_g (\text{minutes}_g)^2}\) within each day). That directly penalizes “Bob hoards Wednesday; Carol is almost idle,” and makes reassignment of e.g. Customer-5 / Customer-3 to Carol attractive when skills and hard constraints allow.

```147:176:src/main/java/org/imd/expertschedule/planner/cp/ExpertPlanningConstraintProvider.java
    private Constraint fairlyDistributePerExpertScheduledItems(ConstraintFactory factory) {
        // medium constraint - penalize imbalance of total diagnosis minutes per expert (whole horizon)
        return factory
                .forEach(ScheduleItem.class)
                .filter(this.populatedScheduleItem())
                .groupBy(FairnessDetector.loadBalance(si -> si.getExpertSchedule().getExpert(),
                                               si -> BigInteger.valueOf(si.getOrder().getDiagnosisDuration().toMinutes())))
                .penalizeConfigurable(result -> fairnessPenaltyMinutes(result))
                .asConstraint(ExpertPlanningConstraintConfiguration.WeightNames.FD_PE_SI_CONFLICT);
    }

    /**
     * Same as {@link #fairlyDistributePerExpertScheduledItems} but per calendar day, so one expert cannot
     * hoard work on a day while another qualified expert stays light (global totals can still look balanced).
     */
    private Constraint fairlyDistributePerExpertPerDayScheduledItems(ConstraintFactory factory) {
        return factory
                .forEach(ScheduleItem.class)
                .filter(this.populatedScheduleItem())
                .groupBy(si -> si.getExpertSchedule().getDate(),
                        FairnessDetector.loadBalance(si -> si.getExpertSchedule().getExpert(),
                                si -> BigInteger.valueOf(si.getOrder().getDiagnosisDuration().toMinutes())))
                .penalizeConfigurable((ignoredDate, result) -> fairnessPenaltyMinutes(result))
                .asConstraint(ExpertPlanningConstraintConfiguration.WeightNames.FD_PE_PD_SI_CONFLICT);
    }
```

Configuration: new weight **`Fairly Distribute Per Expert Per Day Scheduled Items Conflict`** (`FD_PE_PD_SI_CONFLICT`), default `ONE_MEDIUM` like the global fairness constraint.

`./gradlew test` passes.

**Tuning:** If solutions still favor Bob, raise the medium weight for **either** fairness constraint in `ExpertPlanningConstraintConfiguration` (or solver time). Fairness stays **medium**, so it will still yield to **hard** violations; if something cannot move without breaking availability or due dates, the schedule will not “force” your hand-picked split.

---

**User**

Second scheduled item is during the lunch time

========== 2026-03-02 (MONDAY) ==========
--- Expert: Alice ---
Expert skills: Electrical
Expert availability: 2026 | CW10 wd=1 | 10:00 - 14:00
Expert absence: (none for this day)
Expert scheduled items:
  - customer: Customer-6 | due: 2026-03-06 | start: 10:15 | duration: 1h 30m | location: (17.72597, 17.30189) | order skills: Electrical
  - customer: Customer-4 | due: 2026-03-05 | start: 11:45 | duration: 1h 30m | location: (18.16683, 8.63018) | order skills: Electrical

---

**Cursor**

Searching how expert availability and breaks are modeled so we can prevent scheduling during lunch.



Fixing `overlapLunchTime`: it only checked whether the start or end fell inside lunch, so meetings spanning 11:45–13:15 missed lunch entirely. Replacing it with proper interval intersection and adding a hard constraint — lunch was never enforced in the constraint provider.



Here is what was wrong and what we changed.

## What went wrong

1. **Lunch was never a constraint**  
   `PlannerParameters.ExpertRelated` defines lunch as **12:00–12:59**, and `PlannerHelper.overlapLunchTime` existed, but nothing in `ExpertPlanningConstraintProvider` used it, so the solver could place visits across lunch.

2. **`overlapLunchTime` was logically wrong**  
   It only treated lunch as overlapping if the **start or end** of the visit fell inside lunch. A block **11:45–13:15** (your second job) covers lunch, but **11:45** and **13:15** are both outside **12:00–12:59**, so the old check returned **no overlap**.

## What we did

- **`PlannerHelper.overlapLunchTime`** now uses the same **`intersect`** logic as absences: the visit interval is compared to a lunch `DayInterval` on that date, so any real overlap is detected.
- **New hard constraint** `Lunch Time Conflict`: any populated `ScheduleItem` whose interval intersects the configured lunch window is penalized (same pattern as expert availability).
- **Tests** for a “wraps lunch without endpoints inside” interval and for a slot after lunch.

Lunch times still come from ```21:22:src/main/java/org/imd/expertschedule/planner/solution/PlannerParameters.java``` (`lunchStartTime` / `lunchEndTime`). Adjust there if your break differs. After a re-run, the solver should move Customer-4 (or similar) to before lunch, after lunch, or to another expert/day instead of straddling 12:00–12:59.

---

**User**

Help me organize the expert-schedule-benchmark-config.xml using the information from expert-schedule-solver-config.xml which is currently working. 

---

**Cursor**

Fixing the solver XML typo, adding `ExpertPlanningSolutionLoader.loadAsSolution` for benchmarks, implementing `ExpertPlanningSolutionFileIO`, and rewriting the benchmark config to mirror the working solver config.



Here’s what was done.

### 1. **`expert-schedule-solver-config.xml`**
- Removed the bad prefix on the first line so it starts with `<?xml version="1.0" ...` again.

### 2. **`expert-schedule-benchmark-config.xml`** (under `benchmark/` — there is no `expert-scheduling-benchmark-config.xml` in the repo)
- Added a short header comment describing purpose and that the solver block matches the main solver config.
- **`benchmarkDirectory`**: `local/benchmark` (single line).
- **`<inheritedSolverBenchmark><solver>`** aligned with the working solver file:
  - Same commented `environmentMode` / `moveThreadCount` as in `expert-schedule-solver-config.xml`
  - Same `solutionClass`, `entityClass`
  - Same `scoreDirectorFactory` (`ExpertPlanningConstraintProvider`, `ONLY_DOWN`)
  - Same **`minutesSpentLimit`: 2** as in the solver config (with a note you can raise it for long benchmark runs)
- **`<problemBenchmarks>`** no longer references Cloud Balancing:
  - `solutionFileIOClass`: `org.imd.expertschedule.io.benchmark.ExpertPlanningSolutionFileIO`
  - `inputSolutionFile`: `data/expertschedule/dataset-ultrasmall.json` and `dataset-small.json`
- Left **`solverBenchmarkBluePrint` / `EVERY_LOCAL_SEARCH_TYPE`** as before.

### 3. **Supporting code & Gradle**
- **`ExpertPlanningSolutionLoader.loadSolution(...)`** – builds a full `ExpertPlanningSolution` from a dataset file; year / calendar week / working days come from JSON **`metadata`** when present (same idea as `SchedulingApp`).
- **`ExpertPlanningSolutionFileIO`** – implements OptaPlanner’s `SolutionFileIO` for those JSON datasets; `write` only persists a small JSON summary (best score).
- **`build.gradle.kts`**: `optaplanner-persistence-common` and `optaplanner-benchmark` so benchmarks and `SolutionFileIO` resolve on the classpath.

Run the benchmark from the **project root** (so `data/expertschedule/...` paths work), e.g. with a tiny `main` that calls `PlannerBenchmarkFactory.createFromXmlResource("org/imd/expertschedule/benchmark/expert-schedule-benchmark-config.xml").buildPlannerBenchmark().benchmark()`, or from the IDE with working directory = project root. I can add a `BenchmarkApp` + Gradle `run` task if you want that wired in permanently.

---

**User**

The following logic must be outside the loader:
        PlannerParameters plannerParameters = plannerParametersFromMetadata(data.getMetadata());
        return new SolutionInitializer().initialize(plannerParameters, constraintConfiguration, context);
 

---

**Cursor**

Moving solution assembly out of the loader into callers: `ExpertPlanningSolutionFileIO` and `SchedulingApp`.



Adding `PlanningDatasetBundle` and `PlanningSolutionAssembly`, stripping assembly logic from the loader, and updating callers.



Here’s what changed:

### `ExpertPlanningSolutionLoader`
- Only reads JSON and builds domain data.
- **`loadBundle(Path)`** – parses once, returns **`PlanningDatasetBundle(context, metadata)`**.
- **`loadBundleFromDirectory(dataDir, fileName)`** – convenience wrapper.
- Removed **`loadSolution`** and **`plannerParametersFromMetadata`**.

### `PlanningDatasetBundle` (`io.loader`)
- Record: **`SolutionContext`** + **`GeneratorConfig metadata`**.

### `PlanningSolutionAssembly` (`io`)
- **`plannerParametersFromMetadata(GeneratorConfig)`** – same defaults as before (year / CW / working days from metadata when set).
- **`buildUnsolved(PlannerParameters, ExpertPlanningConstraintConfiguration, SolutionContext)`** – delegates to **`SolutionInitializer`**.

### Call sites
- **`ExpertPlanningSolutionFileIO.read`** – `loadBundle` → assembly.
- **`SchedulingApp`** – `loadBundleFromDirectory` → assembly (planner params now come from the dataset **`metadata`**, same as the benchmark path, instead of hard-coded 2026 / CW10).

`./gradlew test` passes.

---

**User**

is it possible to make the medium score closer to zero in case of successfully solved solution?

---

**Cursor**

Checking how medium score is produced in this project:



Explaining: Medium score comes only from the two fairness constraints. With hard=0 the solution is feasible, but medium stays negative unless load balance is perfect — which is uncommon with discrete jobs. Moving fairness to SOFT makes feasible solutions show `0hard/0medium/…` while fairness still ranks solutions on the soft level. Applying that change.



Here’s the distilled answer and what we changed.

### Why medium wasn’t near zero
With **HardMediumSoft**, a *feasible* solution only guarantees **hard = 0**. Your **medium** came **only** from the two fairness constraints (per-period and per-day load balance). Those penalties use \(\sqrt{\sum (\text{minutes per expert})^2}\)-style values, so they are **almost never exactly zero** unless load splits perfectly—which is rare with real skills, windows, and durations.

So: **yes, it’s possible** to have medium at **0** for “good” solutions, but **not** while keeping those fairness goals on the **medium** level, unless the instance happens to be perfectly balanced.

### What we did
Fairness weights were switched from **`ONE_MEDIUM` → `ONE_SOFT`** in `ExpertPlanningConstraintConfiguration`. Then:

- **Feasible** solutions show **`0hard / 0medium / …soft…`** (medium is no longer used for fairness).
- Fairness still matters: it moves to the **soft** component and the solver still prefers fairer plans when hard constraints are satisfied.

If you prefer fairness to stay on medium but be **smaller in magnitude**, keep `ONE_MEDIUM` and instead run longer, improve local search, or **scale down** the fairness penalty in `fairnessPenaltyMinutes` (e.g. divide by a constant)—that pulls medium toward zero without changing tiers.

Revert to medium anytime by setting those two weights back to `HardMediumSoftScore.ONE_MEDIUM` (as noted in the new comment).

---

**User**

Add a cursor rule to use clean code rules whenever new code changes are generated (no more comments on every method or class).

---

**Cursor**

Adding a project Cursor rule next to the existing OptaPlanner core rule (parent repo `.cursor/rules/`).



Added a new always-on Cursor rule:

**`/home/ubuntu/OwnProjects/OptaPlanner-MyExamples/.cursor/rules/clean-code-generated-changes.mdc`**

It tells the agent to:

- Skip routine Javadoc/class-level comments on every type  
- Skip comments that only repeat method or field names  
- Rely on naming and structure first  
- Comment only for non-obvious behavior, invariants, or important public contracts  
- Avoid decorative section banners unless the file already uses that style  

`alwaysApply: true` matches your existing `optaplanner-example-core.mdc`, so it applies to all chats in this project. If you open the repo from the `optaplaner-examples` subfolder only, ensure Cursor’s workspace root is `OptaPlanner-MyExamples` (or copy the same `.mdc` into that folder’s `.cursor/rules/` if you use a separate workspace).

---

**User**

Explain me this from optaplanner documentation:
	
Instead of the squared workload, it is also possible to use the variance (squared difference to the average) or the standard deviation (square root of the variance). This has no effect on the score comparison, because the average will not change during planning. It is just more work to implement (because the average needs to be known) and trivially slower (because the calculation is a bit longer).

When the workload is perfectly balanced, the user often likes to see a 0 score, instead of the distracting -34soft in the image above (for the last solution which is almost perfectly balanced). To nullify this, either add the average multiplied by the number of entities to the score or instead show the variance or standard deviation in the UI.

---

**Cursor**

Here is what that documentation is saying, in plain terms.

## Squared workload vs variance vs standard deviation

Imagine each “bucket” (e.g. expert) has a workload \(L_1, L_2, \ldots, L_n\). During planning you only **reassign** work; you do not change the **total** amount of work or the **number** of buckets. So the **average** load  
\(\mu = \frac{1}{n}\sum_i L_i\)  
is **fixed** for every complete solution.

For that reason:

- **Sum of squares** \(\sum_i L_i^2\)
- **Variance** \(\frac{1}{n}\sum_i (L_i - \mu)^2\)
- **Standard deviation** \(\sqrt{\text{variance}}\)

are all **monotone transforms** of each other (when \(n\) and \(\sum_i L_i\) are fixed). **Better/worse for one is the same ordering for the others.** OptaPlanner only cares about **comparing** solutions, so any of these gives the **same best solution** (same ranking).

Why the docs prefer something like **squared workload** in the engine:

- You only need **per-bucket totals** and \(\sum L_i^2\) (or similar). You do **not** need to compute \(\mu\) in the stream/collector.
- Variance needs the average first (extra step), so it is **more work to implement** and **a bit slower**, with **no better decision** from the solver’s point of view.

So: **same optimization result**, pick the simplest formula in code; use variance/std dev if you care about **interpretation** or **display**, not because the solver finds a different optimum.

---

## Why you might see `-34soft` when it “looks” balanced

The number in the score is whatever your **constraint** computes (e.g. something based on \(\sum L_i^2\) or its square root). That value is often **not** defined to be **0** when things are “fair”:

- Even with **equal** loads \(L_i = \mu\), \(\sum L_i^2 = n\mu^2\) is **not** zero (unless everyone has 0 work).
- With **discrete** jobs, perfect equality is often **impossible**, so the best score is still some negative soft value.

So the UI can look “almost perfectly balanced” while the **raw penalty** is still something like `-34` — that can feel noisy or misleading.

---

## “To nullify this” — two ideas

1. **Change what you add to the score**  
   If you use a formula equivalent to **variance**, then when all \(L_i\) are equal, **variance is 0**, so the **fairness part** of the score can be **0** (for that ideal case).  
   Alternatively, the doc mentions adding a **constant offset** related to “average × number of entities” so that the **displayed** fairness term is shifted — conceptually aligning the metric so the “perfect balance” case lines up with zero (implementation details depend on the exact formula they use in that doc/example).

2. **Don’t show the raw constraint penalty**  
   Show in the UI something humans understand: **variance**, **standard deviation**, **max − min** load, or **“distance from perfect share”**, instead of the internal `-34soft`.

**Summary:** Use the **simplest** load metric in constraints (often sum of squares — no average needed). **Ranking** is the same as using variance/std dev. If you want **0 when perfectly balanced** or clearer numbers for users, adjust the **formula or the UI**, not because the solver would rank differently.

---

**User**

show me a graphic of this explanation. I still do not understand. Give me an example

---

**Cursor**

Here is a **visual + numeric** version of the same idea.

## 1. What stays fixed when you “only reassign” work

Total work and number of experts do not change — only **who** gets **how much**.

```mermaid
flowchart LR
  subgraph fixed["Stays the same"]
    W["Total work W = 9 hours"]
    n["Experts n = 3"]
    mu["Average load μ = W/n = 3"]
  end
  subgraph moves["Planning only changes this"]
    L["Split L₁, L₂, L₃ with L₁+L₂+L₃ = 9"]
  end
  W --> mu
  n --> mu
  L -->|"always"| W
```

So **μ = 3** for **every** complete assignment in this toy example.

---

## 2. Two concrete splits (same total, same μ)

| Assignment (hours per expert) | L₁ + L₂ + L₃ | μ | **Σ L²** (sum of squares) | **Variance**  
\(\frac{1}{3}\sum(L_i-\mu)^2\) | **Std dev**  
\(\sqrt{\text{variance}}\) |
|------------------------------|-------------|---|---------------------------|----------------------------------|---------------------|
| **A — perfectly even** `(3, 3, 3)` | 9 | 3 | \(9+9+9 = 27\) | \(\frac{0+0+0}{3} = 0\) | **0** |
| **B — uneven** `(5, 3, 1)` | 9 | 3 | \(25+9+1 = 35\) | \(\frac{4+0+4}{3} \approx 2.67\) | **≈ 1.63** |

- **Better balance = smaller Σ L² and smaller variance.**  
- Here **A is strictly better than B** on both measures — **same ranking**.

That is what the docs mean: for a **fixed total** and **fixed n**, minimizing **sum of squares** and minimizing **variance** pick the **same best split**; they only **rescale** the number, not the order of solutions.

---

## 3. Why the “score” is often not `0` even when things look fair

**Variance = 0** only when **every** expert has exactly **μ** (here exactly 3 hours each).

But your **penalty in code** might not be “variance”. It might be something like **Σ L²** or **√(Σ L²)**:

- For the **perfect** split `(3,3,3)`: **Σ L² = 27** — **not zero** (everyone still has real work).
- So the solver might show something like **`-34soft`** — a **raw penalty**, not “distance from perfect balance”.

So you can have:

- **Human:** “Looks balanced.”
- **Score:** Still a negative number, because the formula is **not** “0 when fair”, it’s “smaller when fairer than other splits”.

**Ways to get a friendlier “0 when balanced” feeling:**

1. **Use a metric that is 0 when balanced** — e.g. **variance** or **sum of (Lᵢ − μ)²** (with μ known or computed from the same totals).
2. **Or** keep the solver metric as-is, but **in the UI** show variance / std dev / “max − min hours” instead of the raw soft constraint value.

---

## 4. One-line picture: same ranking, different “zero”

```text
        Perfect (3,3,3)          Uneven (5,3,1)
Σ L²         27                      35        ← smaller is better; but 27 ≠ 0
Variance      0                    ~2.67      ← here “perfect” split hits 0
```

So: **squared workload** is easy to implement and **sorts solutions the same** as variance; **variance** is nicer **psychologically** because **perfect balance → 0** in the table above.

If you tell me whether your fairness constraint uses **minutes per expert** and **how many experts** have jobs in a typical day, we can plug in one **real-sized** row like `(120, 90, 90)` vs `(150, 75, 75)` and compute the same three columns.

---

**User**

what is μ ? 

---

**Cursor**

**μ** (mu) is the **mean** (average) workload per expert **when you spread the total work evenly in the arithmetic sense**.

If expert loads are \(L_1, L_2, \ldots, L_n\) (e.g. hours or minutes), then:

\[
\mu = \frac{L_1 + L_2 + \cdots + L_n}{n}
\]

**Example:** three experts with loads `(3, 3, 3)` hours → total \(= 9\), \(n = 3\) → **μ = 9/3 = 3** hours each.

**Important for planning:** if every job is assigned and the number of experts is fixed, **total work** is fixed, so **μ is the same for every complete solution** — only the individual \(L_i\) change when you reassign jobs. That’s why the docs say the average doesn’t change during planning: it’s always **total work ÷ number of experts** (for that fairness grouping).

---

**User**

 To nullify this, either add the average multiplied by the number of entities to the score 

---

**Cursor**

Here **μ** is still the **average load per entity** (per expert, machine, etc.), and **n** is how many entities you’re balancing over.

The doc is pointing at this **algebra** (same total work, same n ⇒ **μ is fixed**):

\[
\sum_{i=1}^{n} (L_i - \mu)^2 \;=\; \sum_{i=1}^{n} L_i^2 \;-\; n\mu^2
\]

- **Left:** sum of squared **distances from the average** → **0** when every \(L_i = \mu\) (“perfectly balanced”).
- **Right:** your **raw sum of squares** \(\sum L_i^2\) minus a term **\(n\mu^2\)**.

When everyone is equal, \(L_i = \mu\) for all \(i\), so:

\[
\sum L_i^2 = n\mu^2
\]

So the **imbalance-only** part is:

\[
\sum L_i^2 - n\mu^2
\]

which hits **0** at perfect balance.

### How that matches “add … to the score”

Wording in docs is easy to misread. **What actually removes the distracting offset** is **not** “add μ×n to the score” in a vague sense, but:

- stop penalizing **\(\sum L_i^2\)** alone, and  
- penalize **\(\sum L_i^2 - n\mu^2\)** (same as **\(\sum (L_i-\mu)^2\)**),

i.e. you **take out** the **\(n\mu^2\)** piece that stays even when balanced.

Note: **μ×n** is the **total** load \(\sum L_i\) (a **constant** during planning). Adding that as a **constant** to the **overall** score would **not** fix the fairness display by itself; the useful identity is the **\(n\mu^2\)** link above.

### Tiny example

n = 3, μ = 3, perfect loads (3,3,3):

- \(\sum L_i^2 = 27\)  ← **not** 0  
- \(n\mu^2 = 3 \cdot 3^2 = 27\)  
- \(\sum L_i^2 - n\mu^2 = 0\)  ← **0 when balanced**

Uneven (5,3,1), still total 9, μ = 3:

- \(\sum L_i^2 = 35\)  
- \(35 - 27 = 8 = \sum (L_i-\mu)^2\)  ← only the imbalance

So: **“nullify”** the big baseline by using **variance-style** deviation (equivalently **\(\sum L_i^2 - n\mu^2\)**), or show that in the UI, instead of raw \(\sum L_i^2\).

---

**User**

Give me another tiny example with 4 numbers

---

**Cursor**

Take **4** experts, **total work = 12 hours**, so with everyone assigned:

\[
\mu = \frac{12}{4} = 3
\]

### Perfectly balanced  
Loads: **(3, 3, 3, 3)**

| Quantity | Value |
|----------|--------|
| \(\sum L_i\) | 12 |
| \(\mu\) | 3 |
| \(\sum L_i^2\) | \(9+9+9+9 = 36\) ← not zero |
| \(n\mu^2\) | \(4 \cdot 3^2 = 36\) |
| \(\sum L_i^2 - n\mu^2\) | \(36 - 36 = 0\) |
| \(\sum (L_i-\mu)^2\) | \((0+0+0+0) = 0\) |

### Unbalanced (same total)  
Loads: **(6, 3, 2, 1)** → sum = 12, **μ = 3** still.

| Quantity | Value |
|----------|--------|
| \(\sum L_i^2\) | \(36+9+4+1 = 50\) |
| \(n\mu^2\) | 36 |
| \(\sum L_i^2 - n\mu^2\) | \(50 - 36 = 14\) |
| \(\sum (L_i-\mu)^2\) | \(3^2+0^2+1^2+2^2 = 9+0+1+4 = 14\) ✓ same |

So with **4 numbers**: balanced → **36** vs **0** depending on whether you look at raw \(\sum L^2\) or the “minus \(n\mu^2\)” / variance-style part.

---

**User**

what is the name of (\sum L_i^2 - n\mu^2)

---

**Cursor**

There isn’t one universal symbol for **\(\sum L_i^2 - n\mu^2\)** alone, but it is **exactly** this standard quantity:

**Sum of squared deviations from the mean** (about the mean):

\[
\sum_{i=1}^{n} (L_i - \mu)^2
\]

because \(\sum L_i^2 - n\mu^2 = \sum (L_i - \mu)^2\).

You’ll also see it described as:

- **SSD** (sum of squared deviations) — common in stats texts  
- Part of **SS** / **sum of squares** in ANOVA-style wording (“around the mean”)  
- Related to **variance**: for **population** variance \(\sigma^2 = \frac{1}{n}\sum (L_i-\mu)^2\), you get  
  \(\sum (L_i-\mu)^2 = n\sigma^2\).

So the short name people use is **“sum of squared deviations (from the mean)”**; \(\sum L_i^2\) alone is often called the **uncorrected** or **raw** sum of squares of the loads, and subtracting \(n\mu^2\) **centers** it so perfect balance gives **0**.

