/**
 * Copyright 2020-2022 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grammar Degree;

/*****************************
 * DataApp (old Degree.g4)   *
 *****************************/

data_app_file:
    data_app_config
    data_app_code
    EOF
;

data_app_config:
    KEYWORD_CONFIGURATION
    (SUB keys+=IDENTIFIER ':' values+=STRING_LITERAL)*
;

data_app_code:
    KEYWORD_CODE
    ('[' (inputs+=block_input_parameter (',' inputs+=block_input_parameter)*)? ']' '->')?
    code=block
;

/*****************************
 * Dataflow                  *
 *****************************/

block
    :   KEYWORD_BEGIN
        (statements+=statement)*
        KEYWORD_END
    ;

block_input_parameter
    :
        name=variable_name ASSIGNMENT_OPERATOR REFERENCE_OPERATOR reference=type_reference
        ('('
           (   functions+=definition_function //TODO hier sind definitiv keine Typreferenzen als argumente erlaubt, was so nicht verboten wird
               (',' functions+=definition_function)*
           )?
       ')')?
    ;

statement
    :   activity_call ';'       #statement_activity_call
    |   if_statement            #statement_if_statement
    |   variable_assignment ';' #statement_variable_assignment
    |   block                   #statement_block
    |   return_statement        #statement_return
    ;

activity_call
    :   (   '['
                (   outputs+=variable_reference
                    (',' outputs+=variable_reference)*
                )?
            ']' ASSIGNMENT_OPERATOR
        )?
        activity=activity_reference
        '['
            (   inputs+=expression
                (',' inputs+=expression)*
            )?
        ']'
    ;

if_statement
    :   KEYWORD_IF '(' conditions+=bool_expression ')' blocks+=block
        (KEYWORD_ELSE KEYWORD_IF '(' conditions+=bool_expression ')' blocks+=block)*
        (KEYWORD_ELSE else_block=block)?
    ;

variable_assignment
    :   name=variable_name ASSIGNMENT_OPERATOR type=type_instantiation                                      #variable_assignment_type_instantiation
    |   name=variable_name ASSIGNMENT_OPERATOR array_values=array_initializer                               #variable_assignment_array
    |   variable=variable_name_index ('.' attributes+=variable_attribute_index)*
                operator=(ASSIGNMENT_OPERATOR | APPEND_OPERATOR)
                variable_value=expression                                                                   #variable_attribute_assignment
    ;

variable_name_index
    :
        name=variable_name ('[' index=INTEGER_LITERAL ']')?
    ;

variable_attribute_index:
        attribute=IDENTIFIER ('[' index=INTEGER_LITERAL ']')?
    ;

type_instantiation
    :   REFERENCE_OPERATOR type=type_reference
        ('('
            (   functions+=definition_function //TODO hier sind definitiv keine Typreferenzen als argumente erlaubt, was so nicht verboten wird
                (',' functions+=definition_function)*
            )?
        ')')?
    ;

array_initializer //TODO erlaubt keine type_instantiation; Umweg über mehrere Variablen, die dann an das array übergeben werden.
    :   '[' (expressions+=expression (',' expressions+=expression)*)? ']'
    ;

return_statement
    :   KEYWORD_RETURN ('[' (return_values+=expression (',' return_values+=expression)* )? ']')? ';'
    ;

BOOLEAN_LITERAL
    :   KEYWORD_TRUE
    |   KEYWORD_FALSE
    ;

/*****************************
 * Common                    *
 *****************************/
qualified_name
    :   (qualifier=IDENTIFIER '.')? name=IDENTIFIER
    ;

activity_reference
    :   reference=activity_name
    ;

activity_name
    :   name=qualified_name
    ;

type_reference
    :   reference=type_name
    ;
type_name
    :   name=qualified_name
    ;

variable_reference
    :   reference=variable_name ('[' index=INTEGER_LITERAL ']')?
    ;

variable_name
    :   name=IDENTIFIER
    ;

//// expressions
expression
    :   method_name=IDENTIFIER '(' expressions+=expression (',' expressions+=expression)* ')'               #expression_method_call
    |   reference=variable_reference ('[' index=INTEGER_LITERAL ']')? ('.' accessedFields+=IDENTIFIER)+     #expression_field_access
    |   reference=variable_reference                                                                        #expression_variable_reference
    |   expression_value=STRING_LITERAL                                                                     #expression_string_literal
    ;

bool_expression
    :   negated='!' expr=bool_expression
    |   '(' expr=bool_expression ')'
    |   left_expression=bool_expression comperator=bool_comperator right_expression=bool_expression
    |   left_expression=bool_expression operator=bool_operator right_expression=bool_expression
    |   integer_value=INTEGER_LITERAL
    |   float_value=FLOATING_POINT_LITERAL
    |   string_value=STRING_LITERAL
    |   bool_value=BOOLEAN_LITERAL
    |   reference=variable_reference
    |   field_reference=variable_reference ('.' accessedFields+=IDENTIFIER)+
    |   '@' method=IDENTIFIER '[' field_reference=variable_reference ('.' accessedFields+=IDENTIFIER)* ']'
    ;

bool_comperator
    :   (operator = '<')
    |   (operator = '>')
    |   (operator = '<=')
    |   (operator = '>=' )
    |   (operator = '==')
    |   (operator = '!=')
    ;

bool_operator
    :   (operator = '&&')
    |   (operator = '||')
    ;

definition_function
    :   AT name=IDENTIFIER '['
        (   arguments+=definition_function_argument
            (',' arguments+=definition_function_argument)*
        )?
        ']'
    ;

definition_function_argument
    :   expression                                          #definition_function_argument_expression
    //all other references are just qualified_name or IDENTIFIER and not distinguishable in the grammar
    |   REFERENCE_OPERATOR reference=qualified_name         #definition_function_argument_reference_by_qualified_name //TODO this breaks _reference exchangability from above: references always must be qualified_names
    ;


//// Keywords
KEYWORD_BEGIN: (B E G I N);
KEYWORD_CODE: (C O D E);
KEYWORD_CONFIGURATION: (C O N F I G U R A T I O N);
KEYWORD_ELSE: (E L S E);
KEYWORD_END: (E N D);
KEYWORD_FALSE: (F A L S E);
KEYWORD_IF: (I F);
KEYWORD_TRUE: (T R U E);
KEYWORD_RETURN: (R E T U R N);

//// Symbols
ASSIGNMENT_OPERATOR: '=';
APPEND_OPERATOR: '+=';
REFERENCE_OPERATOR: '$';
AT: '@';
DOUBLE_QUOTE: '"';
LPAREN: '(';
RPAREN: ')';
LBRACK: '[';
RBRACK: ']';
LBRACE: '{';
RBRACE: '}';
SEMI: ';';
COLON: ':';
COMMA: ',';
DOT: '.';

GT : '>';
LT : '<';
EQUAL : '==';
LE : '<=';
GE : '>=';
NOT_EQUAL : '!=';
NOT : '!';
AND : '&&';
OR : '||';
ADD : '+';
SUB : '-';
MUL : '*';
DIV : '/';
MOD : '%';

//// literals

INTEGER_LITERAL
	:	'0'
	|   ('-')?('1'..'9') ('0'..'9')*
	;

FLOATING_POINT_LITERAL
    :   INTEGER_LITERAL? '.' [0-9]+
    |   INTEGER_LITERAL ('.' [0-9]?)?
    ;

STRING_LITERAL
    :	'"' STRING_CHARACTER* '"'
    ;

fragment
STRING_CHARACTER
    :	~["\\\r\n]
    |	'\\' [btnfr"'\\]
    ;

IDENTIFIER: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;

fragment A : ('A'|'a') ;
fragment B : ('B'|'b') ;
fragment C : ('C'|'c') ;
fragment D : ('D'|'d') ;
fragment E : ('E'|'e') ;
fragment F : ('F'|'f') ;
fragment G : ('G'|'g') ;
fragment H : ('H'|'h') ;
fragment I : ('I'|'i') ;
fragment J : ('J'|'j') ;
fragment K : ('K'|'k') ;
fragment L : ('L'|'l') ;
fragment M : ('M'|'m') ;
fragment N : ('N'|'n') ;
fragment O : ('O'|'o') ;
fragment P : ('P'|'p') ;
fragment Q : ('Q'|'q') ;
fragment R : ('R'|'r') ;
fragment S : ('S'|'s') ;
fragment T : ('T'|'t') ;
fragment U : ('U'|'u') ;
fragment V : ('V'|'v') ;
fragment W : ('W'|'w') ;
fragment X : ('X'|'x') ;
fragment Y : ('Y'|'y') ;
fragment Z : ('Z'|'z') ;


//// Skipped lexer elements
ML_COMMENT: ('/*' .*? '*/') -> skip;
SL_COMMENT: ('//' .*? NL) -> skip;
WS: (' ' | '\t')+ -> skip;
NL: ('\r'? '\n') -> skip;
