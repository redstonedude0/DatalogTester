Hi guys!
I thought it'd be good to have a central file for noting down what we're working on, or how things we've written work, etc

Notes for anyone working on the graql executor (Gabriele):
- Please include the grakn/graql libraries inside the /lib/ folder (next to junit and hamcrest)

Notes for anyone working on the graql->ast transformation (Charles):
- I've moved the AST stuff to uk.ac.cam.gp.charlie.ast


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




