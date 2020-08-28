## Overview

The `sts-cli` project is an executable (command line) tool for running CEGAR-based analyses on Symbolic Transition Systems (STS).
For more information about the STS formalism and its supported language elements, take a look at the [`sts`](../sts/README.md) project.

### Related projects

* [`sts`](../sts/README.md): Classes to represent STSs and a domain specific language (DSL) to parse STSs from a textual representation.
* [`sts-analysis`](../sts-analysis/README.md): STS specific analysis modules enabling the algorithms to operate on them.

## Using the tool

1. First, get the tool.
    * The easiest way is to download a [pre-built release](https://github.com/ftsrg/theta/releases).
    * You can also [build](../../doc/Build.md) the tool yourself. The runnable jar file will appear under _build/libs/_ with the name _theta-sts-cli-\<VERSION\>-all.jar_, you can simply rename it to _theta-sts-cli.jar_.
    * Alternatively, you can use our docker image (see below).
2. Running the tool requires Java (JRE) 11.
3. The tool also requires the [Z3 SMT solver libraries](../../doc/Build.md) to be available on `PATH`.
4. The tool can be executed with `java -jar theta-sts-cli.jar [ARGUMENTS]`.
    * If no arguments are given, a help screen is displayed about the arguments and their possible values.
    More information can also be found below.
    * For example `java -jar theta-sts-cli.jar --model counter.system --loglevel INFO` runs the default analysis with logging on the `counter.system` input file.

### Docker

A Dockerfile is also available under the _docker_ directory in the root of the repository.
The image can be built using the following command (from the root of the repository):
```
docker build -t theta-sts-cli -f docker/theta-sts-cli.Dockerfile .
```

The script `run-theta-sts-cli.sh` can be used for running the containerized version on models residing on the host:
```
./docker/run-theta-sts-cli.sh model.sts [OTHER ARGUMENTS]
```
Note that the model must be given as the first positional argument (without `--model`).

## Arguments

All arguments are optional, except `--model`.

* `--model`: Path of the input STS model (mandatory).
* `--cex`: Output file where the counterexample is written (if the result is unsafe). If the argument is not given (default) the counterexample is not printed. Use `CON` (Windows) or `/dev/stdout` (Linux) as argument to print to the standard output.
* `--loglevel`: Detailedness of logging.
    * Possible values (from the least to the most detailed): `RESULT`, `MAINSTEP`, `SUBSTEP` (default), `INFO`, `DETAIL`, `VERBOSE`
* `--domain`: Domain of the abstraction, possible values:
    * `PRED_CART`: Cartesian predicate abstraction (default).
    * `PRED_BOOL`: Boolean predicate abstraction.
    * `PRED_SPLIT`: Boolean predicate abstraction with splitting.
    * `EXPL`: Explicit-value abstraction.
    * _Remark: Predicate abstraction tracks logical formulas instead of concrete values of variables, which can be efficient for variables with large (or infinite) domain.
  Explicit-values keep track of a subset of system variables, which can be efficient if variables are mostly deterministic or have a small domain.
  Cartesian predicate abstraction only uses conjunctions (more efficient) while Boolean allows arbitrary formulas (more expressive).
  Boolean predicate abstraction often gives predicates in a disjunctive normal form (DNF).
  In `PRED_BOOL` this DNF formula is treated as a single state, while in `PRED_SPLIT` each operand of the disjunction is a separate state._
    * _Remark: It is recommended to try Cartesian first and fall back to Boolean if there is no refinement progress (seemingly infinite iterations with the same counterexample).
  Splitting rarely resulted in better performance._
    * _More information on the abstract domains can be found in [our JAR paper](https://link.springer.com/content/pdf/10.1007%2Fs10817-019-09535-x.pdf), Sections 2.2.1 and 3.1.3._
* `--initprec`: Initial precision of the abstraction.
    * `EMPTY`: Start with an empty initial precision (default).
    * `ALLVARS`: Track all variables by default (only applicable if `--domain` is `EXPL`).
* `--search`: Search strategy in the abstract state space, possible values:
    * `BFS` (default), `DFS`: Standard breadth- and depth-first search.
* `--refinement`: Refinement strategy, possible values:
    * `FW_BIN_ITP`: Forward binary interpolation, only performs well if `--prunestrategy` is `FULL`.
    * `BW_BIN_ITP`: Backward binary interpolation (see Section 3.2.1 of [our JAR paper](https://link.springer.com/content/pdf/10.1007%2Fs10817-019-09535-x.pdf) for more information.
    * `SEQ_ITP` (default): Sequence interpolation.
    * `MULTI_SEQ`: Sequence interpolation with multiple counterexamples (see Section 3.2.2 of [our JAR paper](https://link.springer.com/content/pdf/10.1007%2Fs10817-019-09535-x.pdf) for more information).
    * `UNSAT_CORE`: Unsat cores, only available if `--domain` is `EXPL`.
    * _Remark: `BW_BIN_ITP` and `SEQ_ITP` has the best performance usually._
* `--predsplit`: Splitting applied to predicates during refinement, possible values:
    * `WHOLE` (default): Keep predicates as a whole, no splitting is applied. Can perform well if the model has many Boolean variables.
    * `CONJUNCTS`: Split predicates into conjuncts.
    * `ATOMS`: Split predicates into atoms.
* `--prunestrategy`: Pruning strategy during refinement, possible values:
    * `FULL`: The whole ARG is pruned and abstraction is completely restarted with the new precision.
    * `LAZY` (default): The ARG is only pruned back to the first point where refinement was applied.

### For developer usage

| Flag | Description |
|--|--|
| `--benchmark` | Benchmark mode, only print metrics in csv format. |
| `--header` | Print the header for the benchmark mode csv format. |
