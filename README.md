# Food Trucks
This document describes a prototype of the Food Trucks project. The technical track chosen is "backend", mostly because
I did not have enough time to write a particularly nice front end as well, although the frontend is fully functional.

The result may be viewed here: [Food Truck Finder demo](http://food-truck-finder3.elasticbeanstalk.com/index.html)

## Language, database and libraries used
I decided to use [clojure](http://clojure.org) for the prototype. This is a language I am not particularly skilled in.
 I have read a [book](https://pragprog.com/book/shcloj2/programming-clojure) about it and solved a number of exercises 
 on [4clojure.com](http://www.4clojure.com), but that was a couple of years ago. I have never written a web application 
 (or any other application) in clojure.
 
The rationale behind this decision is that I thought it would be rather uninteresting to code a project like this using
a platform well known to me (e.g. NodeJS). I have wanted for quite a while to try to code a web application in Clojure,
and this coding exercise was the perfect excuse.

I find Clojure interesting for a number of reasons:

 - Its a dynamic, strongly typed language, but with an [optional type sytem](http://typedclojure.org/)
 - It is a functional language - all the built in datastructures are [persistent](http://clojure.org/data_structures)
 - It is a LISP, and as such has a powerful [macro facility](http://clojure.org/macros)
 - Clojure strongly emphasizes [simplicity](http://www.drdobbs.com/architecture-and-design/the-clojure-philosophy/240150710).
 - It has some very interesting [concurrency features](http://clojure.org/concurrent_programming), including 
 [core.async](https://github.com/clojure/core.async), a library implementing CSP using lightweight cooperative threading
 - It can be hosted on either the JVM or [JavaScript](https://github.com/clojure/clojurescript)
 - It is  performant compared to other dynamic languages, and provides many optimization options for performance hot spots
 - It has simple and elegant interoperability with the host platform, so it is very easy to use existing JAVA or 
 JavaScript libraries
 
I decided to use PostgreSQL as database. The reason behind this decision is that it is familiar to me (trying out a 
somewhat unfamilliar language is challenging enough!), and that it supports
[GIS](http://postgis.net/) features such spatial indexes for finding objects near a particular geographic location.

I decided not to use any complex high-level frameworks for the web application. I use [ring](https://github.com/ring-clojure/ring),
 which is the clojure equivalent of NodeJS [Connect](https://github.com/senchalabs/connect), and 
 [Compojure](https://github.com/weavejester/compojure) which is the clojure equivalent for NodeJS 
 [Express](http://expressjs.com/). 
 
I used JAVA JDBC for connecting to the PostgreSQL. JDBC is an extremely unpleasant API, but fortunately there is a 
[clojure library](https://github.com/clojure/java.jdbc) which provides a much more pleasant API on top of JDBC. 

A high-level library (such as an ORM) is not used for accessing the database. JAVA ORMs such as 
[Hibernate](http://hibernate.org) is problematic enough in JAVA, and completely unsuitable for Clojure. There 
are some high-level 
Clojure libraries ([1](http://sqlkorma.com/), [2](https://github.com/jkk/honeysql), [3](https://github.com/r0man/sqlingvo)) for dealing with SQL databases. Of these, [SQLingvo](https://github.com/r0man/sqlingvo)
 seems suitable for this project and could probably have been used with success (and less custom code) as a result.
 
The de-facto build tool of choice for clojure applications is [Leiningen](https://github.com/technomancy/leiningen). 
Leiningen is a declarative build system similar to JAVAs [maven](http://maven.apache.org/), but it seems a lot more 
pleasant and simple to work with.

For the frontend, I decided to use plain old JavaScript - no cross compilation. I display the map using 
[Leaflet](http://leafletjs.com/), a library I have no prior experience with. I would have liked to create
a more sophisticated frontend, but unfortunately the clojure backend took up too much time.

## The clojure learning experience
Although the clojure language and platform has many interesting features, it turned out to be a somewhat challenging 
language to learn to use effectively for a web application:

 - The language is completely different from languages like JAVA and JavaScript. There is no class concept,
 everything is data and functions. This, combined with immutable data structures, requires a completely different
 way of thinking about programming than traditional object-oriented languages.
 - The syntax is hard to read. It is probably mostly due to the fact that I have only been programming 
 [Curly bracket languages](http://en.wikipedia.org/wiki/List_of_programming_languages_by_type#Curly-bracket_languages)
 for my entire professional career, but it might also be partially due to the lack of visual variation in the clojure 
 syntax.
 - It is necessary learn a moderately large [vocabulary](https://clojure.github.io/clojure/clojure.core-api.html) of
 basic functions in order to work effectively with the Clojure's persistent datastructures - figuring out
 which basic function to use for a particular task is not easy.
 - Even [editing](https://cursiveclojure.com/userguide/paredit.html) clojure code the idiomatic way has a learning curve!
 
## API documentation
The food trucks web app implements a REST API with a single HTTP resource `/foodtrucks`, which is the list
 of food trucks in SF. The following query parameters may be supplied:
 
 - `x`, `y`: The resulting list of food trucks will be ordered according to the distance to `(x,y)`, where `x` is the 
 longitude and `y` is the latitude.
 - `limit`: Only the specified number of food trucks will be returned.
 - `status`: Only return food trucks with the given status.
 
The resource returns a JSON-array of objects, formatted like this:

```json
{
  "id": 1,
  "x": -122.394064145441,
  "y": 37.7841781516735, 
  "foodItems": "All Food Items", 
  "status": "POSTAPPROVED", 
  "facilityType": "Truck", 
  "applicant": "SAS Group LLC", 
  "locationId": 551644 
}
```

## Code overview
The application consists of three namespaces.

- `foodtrucks.sql`: Som general functionality for building SQL query strings.
- `foodtrucks.parameter`: Som general functionality for parsing HTTP query parameters.
- `foodtrucks.core`: The application.

Additionally, there is a clojure script `scripts/load-data.clj` which takes a CSV file of
food truck data and loads it into the database.

### Coding style
I try to stick to idiomatic clojure. In general, I prefer a coding style where I separate the "what" from the "how". More
specifically, i try to specify the "what" using a declarative data structure (DSL), and the "how" is a function that 
interprets the data structure. For example, the set of valid query parameters for the /foodtrucks resource is specified 
like this:

```clojure
  { :x { :type :double }
    :y { :type :double }
    :limit { :type :integer }
    :status {:type :string}
    :id { :type :integer }}
```

The advantage of this approach is that the encoded information is easily readable and concise, and the information can 
be used in many ways. For example, the information above could be used to generate or verify API documentation.

### The foodtrucks.sql namespace
The `foodtrucks.sql` namespace provides som basic functions for manipulating SQL strings. It is quite limited,
 and only includes the functionality required to correctly form the SQL queries needed for the project.
 
### foodtrucks.parameter
The `foodtrucks.parameter` namespace provides some functions to parse the HTTP query parameters, which are supplied
as strings. It is also very basic, and currently only handles parsing of string, integer and double parameters.

### foodtrucks.core
This namespace contains everything else. It contains a description of the database model, a specification of the
supported query parameters, some functions that can apply the supported query parameters to a SQL statement, and
finally these parts are combined into a complete Ring handler for the `/foodtrucks` resource.

### Frontend
The frontend resides in the directory `lib/resources`, and consists of a single HTML
file `index.html` as well as a JavaScript file `food-truck-finder.js`.

The tool [Bower](http://bower.io/) is used for downloading frontend dependencies (Leaflet and JQuery).

### Tests
Unit tests are supplied for functions suitable for unit testing. There is currently no integration tests. In order for 
the application to be of production quality, integration tests would definitely have to be added. At least the following
should be tested:

 - The `SELECT` clauses generated actually works when executed against the database (that is, the table specification
 in `foodtrucks.core` is consistent with the actual database layout).
 - The `WHERE` clauses generated when supplying coordinates actually gives the expected results when executed against 
 the database.
 - The `/foodtrucks` resource returns the expected JSON results, Content-Type, etc.
 
## Running the application

### Prerequisites
In order to run the application the following is required:

 - PostgreSQL
 - JAVA (7 or higher)
 - NodeJS
 - Bower (`npm install -g bower`)
 - [Leiningen](http://leiningen.org/#install)
 
### Download dependencies
Install Backend dependencies:

```bash
lein deps
```

Install Frontend dependencies:

```bash
lein bower install
```
  
### Create the database
Set up a database user.

Run the SQL script `scripts/createdb.sql` to create the `foodtrucks` database:
 
```bash
psql < scripts/createdb.sql
```
 
### Load the data 
Execute the script `scripts/load-data.clj` to load data into the database, substituting
the correct db username and password in the database connection URI below: 

```bash
DB_URI="jdbc:postgresql://localhost/foodtrucks?user=<db-user>&password=<db-password>" lein exec -p scripts/load-data.clj data/Mobile_Food_Facility_Permit.csv  
```

### Run the tests
Run the tests using leiningen:
```bash
lein test
```

### Start the web server
Start the web server on port 4000:

```bash
DB_URI=<dburi> lein lein ring server-headless 4000
```

### Build the deliverable
Build a JAVA Web Archive (WAR):

```bash
lein ring uberwar
```

A WAR file requires a servlet container to run. You can also build a standalone JAVA executable JAR:
```bash
lein ring uberjar
```

Run this using
```bash
DB_URI=<dburi> java -jar <jarfile>
```

## TODOs before the prototype could be considered production quality
The following would have to be added before this prototype could be considered production quality:

 - Integration tests
 - Correct HTTP response code and reasonable error message when invalid parameters are supplied
 - Database connection pooling
 - Appropriate and configurable logging
 - Appropriate HTTP caching headers on responses
 - The script to load data into the database is very slow, and would not
 work for large amounts of data. The script could be rewritten using the PostgresSQL 
 [COPY](http://www.postgresql.org/docs/9.3/static/sql-copy.html) feature for much better performance.
 
## Things I would do differently
 - I would probably use a 3rd party tool ([SQLingvo](https://github.com/r0man/sqlingvo)) for generating
 SQL strings