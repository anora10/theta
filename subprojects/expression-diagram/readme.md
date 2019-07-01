Class diagram can be seen in file class.png. Here follows a short description of classes.

ExpressionNode:
The most important class. It stores an expression, and a map which maps ExpressionNodes to Literals, which contain the resulting expression of the substitution.

VariableSubstitution:
When substituting a variable, it checks whether the resulting expression is already known, by searching for it in the UniqueTable. It worksas a linked list, having a pointer for the instance with the following variable.

UniqueTable:
The table contains a map, mapping expressions to ExpressionNodes, to show, which expressions are already reached. To each variable there is a UniqueTable connected.

Cursor:
Inner class of ExpressionNode class. Function moveNext() finds a new possible substitution value for the ExpressionNode, or returns, if it is not possible (all the satisfying substituting values for the variable are already found and stored in its map).

Solver:
In this module the Solver class is imported and used to find satisfying substitution values for the expression that belongs to the cursor.
