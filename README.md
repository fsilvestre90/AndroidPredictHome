# BeerMe

# Introduction

This project is for the Urban Airship interview project.

# Overview

I used the [Dropwizard](http://www.dropwizard.io/1.0.6/docs/) library to complete this excercise.

How to start the BeerMe application
---

1. Run `mvn clean install` to build your application
1. Create DB with `java -jar target/BeerMe-1.0-SNAPSHOT.jar db migrate config.yml`

1. If you want to skip those, just type `./clean` to kickstart a script which does that for you

1. Start application with `java -jar target/BeerMe-1.0-SNAPSHOT.jar server config.yml`
1. To check that the application is running enter url `http://localhost:9000`

I'm new to dropwizard/building services in Java. When I first started, I wasn't aware of h2 database. I went back and rewrote the DAO classes to utilize the flexibility of a database.

# If you had to do this project again, what would you do differently and why?
    What I would change:
    1) More unit tests. I feel like I could get more granular once I have more experience with junit. I would also have more cases!
    2) Better error handling/messaging. What if somebody didn't give a complete body for 'addBeer'? I would include a message indicating what the user missed
    3) More endpoints. I spent a majority of my time learning this framework and configuration. Now that I understand this framework, I can build the product quicker next time.
    4) More filtering - find a beer based on a name
    5) More fields - count of how many beers a brewery has, how many beers are in the collection, etc


#  What features did you choose to implement and why?

    Features I chose:
      1) Dropwizard: I did not want to reinvent the wheel. This was a great tool to use! I was able to create config file, easily attach/detach endpoints, and more. It's really stable.

      2) This projects features contain many libraries UA uses based on the job description. E.G. Jersey, Jackson, and Jetty.

      3) When I first wrote the app I manually created objects instead of using a DB. I went back and implemented Hibernate.
            - I was able to create a migration file and sql scripts to create & populate the test database
            - I could create a join table which tracked new beers that a brewery made


# Endpoints
    GET     /beer/all                    # Get all beers
    GET     /beer/get/{id}               # Get a beer with ID
    DELETE  /beer/remove/{id}            # Remove a beer with ID
    POST    /beer/save                   # Add a new beer
    GET     /beer/style?q={query}        # Filter beer based on a style
    PUT     /brewery/addBeer/{id}        # Add a beer to the brewery
    GET     /brewery/all                 # Get all breweries
    POST    /brewery/save                # Add a new brewery
    GET     /brewery/{id}                # Get a brewery on ID
