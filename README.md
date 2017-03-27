# AndroidHomeFinder

# Introduction

This project is for the InMarket interview project.

# Overview

I used the apache math commons library to utilize the kmeans algorithm. The app saves users location to a database for future calculations.

# If you had to do this project again, what would you do differently and why?
    This project seems more efficient if it were done with a web server. I would create a web service that accepts data from clients and sends it to a spark service. Our client would send coordinates, timestamp, and activity (biking, walking, still, etc). The spark job would filter through a decision tree model using a machine learning library. This would output the predicted location and confidence level.

#  What features did you choose to implement and why?

    Due to the 2 hour time constraint, I thought the best approach would be kmeans clustering method. It's quick and doesn't require a large data set to model. We could easily compare the size of each centroid and make a good guess.
