# LanguageDesign
Declaring Variables
--------------------
Using the Type_Conversion example, to declare a variable you state its type, an identifier, the "=" assignment operator and then a value. 
Constant variables are also declared in the same way with the constant reference  "const" being placed before the type e.g. const int i = 5. 
Variables must be declared with the right data type so an int cannot be initialised with a double value nor a double with an int value. 
A string's value must be formatted with double quotes. Variables must be assigned a value at declaration excect for lists, this value can be 
in the form of a literal, variable or the return from a function as long as the types match the variable types. 
Variables can also be assigned to after declaration and types can be implicitly converted between variables e.g. if I wanted a double value 
from a literal or a variable placed into an int it will automatically convert on assignment. Strings however are a little different as different 
types can be converted into a string but a string cannot be converted into different types.
Taking a look at the Calc_Average example lists are declared a little differently in that you have to enclose the type in the list declaration e.g. 
List<int>. List values must also be enclosed with square brackets e.g. List<int> list = [4,6,7]. Variables can also be used to initialise a list and 
even functions as long as they are entered in between the curly brackets, however if the variable being used to add data to the list contains a list 
or a function returns a list then the list being declared can only have one value in the square brackets which is the returned list or the variable 
that contains a list. You cannot initialise a list with multiple lists or a list and then other values. Lists are also not able to be declared as const. 
Negative numbers can be given in variables and list variables except for variables or lists of type bool, string lists and variables will take 
them but they will need to be enclosed with "" so they will not be stored as negative value just a normal string.

Using Variables
----------------
Variables can be used after declaration for the reassignment of data unless they are a constant. Looking again at the Type_Conversion example to 
reassign a variable you type in the variable id followed by the assignment operator "=" and then a value or a variable id, you can also reassign using a function. 
This operates differently to declaring a variable in that if the type of the data is not the same type as the variable you are assigning to, this data will 
automatically converted to the data type of the receiving variable. This is the same for lists however string variables and lists act a little differently as 
they can have other types converted into them but they cannot be converted to other types e.g. an int can be converted to a string but a string cannot be converted into an int.   

Loops and if statements
------------------------
Looking at the Sort_List_Integer example, the loops available are the for and while loops and the control statements available are if else statements.
These statements operate mostly the same as in Java and C++. 
The for loop is declared using the FOR keyword needs 3 inputs, the first being a variable to act as the iterator, it then needs a condition to assess 
and finally it needs an increment value. This increment value is not something like i++ or i-- it is an integer number, so if 1 was given then one would 
be added to the variable each time, if 2 then 2 added each time and so on. Negative numbers can also be given as increment values. Variables or plain literals 
can be used as inputs for the for loop and the iterator input can be the declaration of a new variable, when passing an already existing variable as the 
iterator value it will need to be reassigned in the for loop declaration so a const variable cannot be used as the iterator variable. If you did not want to 
change the value of the variable you are using as the iterator just type variable = variable so it is reassigned to equal the value it already had. 
There are two different conditions that can be assessed which are variable > value or variable < value.
While loops operate the same as they do in Java and C++ and are declared using the WHILE keyword. There are 6 different conditions that can be assessed 
which are ==, !=, >, <, <=, >=. Numbers of different types can be compared however strings can only be compared with strings and the only conditions they will work In and != and ==.
If statements operate the same as the while loops except they are declared using the IF keyword. An if can also have an else statement after it using the ELSE keyword, 
these ELSE statements cannot take parameters. If else statements are not available and IF and ELSE statements must start and end with curly brackets like in java and C++.
  
Functions
---------
Using the Calc_Average example, functions are declared with their return type which can also be void indicating it returns nothing, an identifier which has to be unique 
and brackets either containing nothing or variable declarations. These declarations however only need to declare a type and an id. Functions must start and end with curly 
brackets and function the same as in C++ or Java but do not have the power to be linked to an object and if a function has declared a return type then it must return something 
using the RETURN keyword and this something has to be of the type specified in the function declaration. Void functions cannot return a value.

Scoping
-------
Variables are dynamically scoped and so multiple instances of the same identifier can be used as long as they are in different scopes (see Sort_List_Integer). 
If identifiers of the same name have been created then the first instance of a variable with that name will be used when looking above the place where that variable 
has been referenced e.g. if a variable x was declared and then an new variable x was created inside a function, IF statement etc. then if x was called in the function etc. 
it would be the second instance of x that would be used until the function etc. was left then any call of variable x would go to the first x and the second x would not exist.
  
Library Functions 
-----------------
The library functions that available are as follows:

LIST_GET() - takes a list id and an integer (either a literal or a variable) as the index of the item in the stated list to return. A non-integer number cannot be used 
as an index position. (See List_Control).

LIST_SETAT() - takes a list id, an integer (either a literal or a variable) as the index of the item in the stated list to return and a new value to enter. A non-integer 
number cannot be used as an index position. Implicit type conversion is available except if a string is used as the new value. (See List_Control)

LIST_REMOVEAT() - takes a list id and an integer (either a literal or a variable) as the index of the item in the stated list to remove. A non-integer number cannot be 
used as an index position. (See List_Control).

LIST_ADD() - takes a new value to enter, implicit type conversion is available except if a string is used as the new value. (See List_Control).

LIST_SIZE() - does not take any inputs and is simply a statement to return the size of a list (See List_Control).

LIST_CLEAR() - does not take any inputs and is simply a statement to delete all data within a list (See List_Control).

ADD() - takes two values to add together and returns the result. The result will always be a double which then gets converted into the type of the receiving variable if there is one. 
(see Calc_Average example for idea of how to use function).

MUL() - takes two values to multiply together and returns the result. The result will always be a double which then gets converted into the type of the receiving variable if there is one. 
(see Calc_Average example for idea of how to use function).

DIV() - takes two values to divide and returns the result. The result will always be a double which then gets converted into the type of the receiving variable if there is one. 
(see Calc_Average example for idea of how to use function).

SUB() - takes two values to subtract and returns the result. The result will always be a double which then gets converted into the type of the receiving variable if there is one. 
(see Calc_Average example for idea of how to use function).

PRINT() - can take multiple inputs and will print out the results. If multiple inputs are given then the output will be the concatenation of all the values passed e.g. 
PRINT(3) will print 3 whereas PRINT(3,4) will print 34 whereas PRINT(3,",",4) will print 3,4. An entire list can be printed by just entering in the list's id.