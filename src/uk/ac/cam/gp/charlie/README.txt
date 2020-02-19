Hi guys!
I thought it'd be good to have a central file for noting down what we're working on, or how things we've written work, etc

Notes for anyone working on the graql executor (Gabriele):
- Please include the grakn/graql libraries inside the /lib/ folder (next to junit and hamcrest)

Notes for anyone working on the graql->ast transformation (Charles):
- I've moved the AST stuff to uk.ac.cam.gp.charlie.ast
- I think we need to tweak the AST stuff a bit, rather than having different ASTs for the schema, data,
  and tests. I think it'd be good to have a generic AST which represents one Graql query, that way
  when it comes to building an interactive interface it'll be much easier to handle that input, and the
  tests are less constrained too. I've started to implement some AST structure, we'll need to work on this
  together properly, your main task will be to work on the GraqlParser -
    currently I'm thinking this should produce a list of Statements from a string, but I'm considering
    making it take a character stream and return a statement stream instead so that we can have multiline
    input easier in interactive mode? I'm not 100% sure what's best, the choice is up to you.


Notes for all:
- I've added a TestLoader class to load and parse tests, this will definitely need changing, I just threw it together for testing

The rough data flow (for now) is as follows. We should modify this so that the testloader runs tests on both executions simultaneously and compares results.
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
- the syntax has been augmented with the idea of variables being bound in scope, e.g.
  "insert $x isa person; $y isa person; (friend:$x, friend:$y) isa friendship;" is valid. This allows the interactive
  interface to be smoother, and is how insertion within match queries (and rules) is calculated.


