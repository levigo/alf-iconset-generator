# ALF Icon Set Generator

This project provides a simple generator for icon sets for the ALF format of the [jadice document platform](http://jadice.com) as XML format.

## Usage

 * Clone this repository
 * Build the project with Apache Maven 3 and java 8 (``mvn install``)
 * Provide the icons in a dedicated folder ``<icon-source-folder>``
 * Run it with ``java -jar alf-icon-generator.jar  <icon-source-folder> <target-file>``
 * ``<target-file>`` will contain the resulting XML file 

## Conventions

* The icons must be provided as PNG or GIF
* Their file names shall match the intended ID (2 digits or uppercase letters)   
Example: ``AA.png`` will be used as icon for the ID ``AA``

## Examples

The folder ``src/test/resources`` contains some example icons and the resulting XML icon set


## License

This project is licensed under the BSD (3-clause style).  
Pull requests are appreciated