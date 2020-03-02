Group Charlie Datalog Tester
Run Workbench for interactive testing

The rough data flow (for now) is as follows:
- Main method calls TestLoader, which loads from a specified file.
- TestLoader constructs a TestEnvironment from the schema and data parts of the file.
- TestLoader constructs an Executor from the TestEnvironment
- The constructor should create any necessary connections in the case of graql, and in the case of datalog it:
  - Converts the environment into a context by passing it in 2 bits to the graql parser
  - the context constructor will perform analysis on the ASTs and generate datalog code (using astinterpreter)
  - the constructor stores parsed datalog ready for tests to be run
- the constructors will return executor objects
- TestLoader passes test strings individually to the executor
- the graqlexecutor will execute these queries and return the results in a result object
- the datalogexecutor will parse these (along with the context) into datalog (using astinterpreter), compile it into an engine, execute, and return as a result object.

Overview of quirks about the datalog implementation:
- the engine assumes correct queries, incorrect queries (syntactically or semantically) have undefined behaviour.
- the engine implements a subset of graql, some queries will lead to undefined behaviour.
- the datalog engine is only instantiated on match.. queries, as follows;
  - first the validity of all rules are checked, for any rules where their invariant is broken appropriate
    match..insert queries are executed to repair the invariant, if the invariants are not repaired within
    (default: 10) iterations, then the query fails.
  - then the match is executed, producing a list of maps.
  - then this is used to either delete, insert, or get.
- the engine automatically 'commits' on each match query, this is the point at which rule invariants are checked and fixed

TESTS MEANT TO FAIL:
large_employed.test (6/6) - rules currently allow insertion of illegal data
  Expected: {0,5},{5,0},{0,1},{1,0},{5,1},{1,5},{3,4},{4,3}
targetedtest_1.test (1/1) - same as large_employed.test
  Expected: {0,1},{1,0},{0,2},{2,0},{1,2},{2,1}CHECK
duplicants.test(4/7)
  Expected: {0,3},{3,0},{1,0},{0,1},{2,5},{5,2},{1,3},{3,1}
