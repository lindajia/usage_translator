# Third-party dependencies

Third party dependencies of this project, and the dependencies' licenses are as follows:
1. opencsv - Apache License. Included for csv parsing.
2. lombok - MIT License. Used for auto getter generation.
3. jackson-databind - Apache License. Added for json parsing.
4. slf4j-log4j12 - MIT License. Added for logging support.

# Security concern

Current code does String escape when generating SQL statement, which could help mitigate some SQL exploits.
In a realistic scenario, a prepared statement would be used instead.

# SQL output

SQL batch insert statements are saved to chargeable.txt and domains.txt.
