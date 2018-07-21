
# MapReduce Moive Recommender System
<script type="text/javascript" src="http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=default"></script>

## Overview

This is a movie Recommender System project based on [Item Collaborative Filtering](https://en.wikipedia.org/wiki/Item-item_collaborative_filtering). It is run on Hadoop MapReduce Cluster environment. This project is still in progress. More experiments and results will be released in this resporitory further.

## Dataset and Preprocess

The dataset used for this movie Recommender System comes from the [Netflix Prize](https://www.netflixprize.com/). The original Netflix dataset consists of movie ratings from 480189 users and 17770 movies, which is too large to run on Pseudo-Distrubuted Mode. As a result, a parameter **n** can be specfied to select the first **n** movies from the original training set. 20% of the training data will be split for testing set (See the ```data_preprocess.py``` file for more details). All the selected data will be written into a single txt file as the raw input data for MapReduce job. The input format for both train and test data is:

<code>   ** userID,movieId,rating **
</code>

## Algorithms

The Item Collaborative Filtering Algorithms can be divided into two main steps

### 1.Compute the similarity between items

The most commonly used metric for items is Cosine Similarity

<center> $$ W_{ij} = \frac{|N(i)| \bigcap |N(j)|}{\sqrt{|N(i)||N(j)|}} $$ </center>

in which $ |N(i)| $ is the number of users interested in item ```i```, $ |N(j)| $ is the number of users interested in item ```j```. $|N(i)| \bigcap |N(j)|$ is the number of user interested in both item ```i``` and ```j```

### 2. Compute prediction ratings for users' rating history

After acquiring the similarities between each pair of movies, we can make predictions by users' rating history. The rating of user ```u``` to item ```j``` is calculated as below

<center> $$ P_{uj} = \frac{\sum_{i \in N(u)} w_{ji}r_{ui}}{\sum_{i\in N(u)}w_{ji}}  $$

in which $ N(u)$ is the set of items rated by user ```u```. $w_{ji}$  is the similarity between item ```i``` and ```j```, $r_{ui}$  is the previous rating of item ```i``` from user ```u```

This Recommender System was finally evaluated on test dataset by the Root Mean Square Error
<center> $$ RMSE = \sqrt{ \frac{1}{n} \sum_{uj} (P_{uj} - R_{uj})^2} $$

in which, $ R_{uj}$ is the ground truth rating of user ```u``` to movie ```j```, $ P_{uj}$ is the prediction made by recommender sytem, ```n``` is the total number of test dataset.

There are total 8 MapReduce jobs used for implementing this algorithm in Hadoop distributed file system. 

* ```DataDivideByUser.java```: Split raw-input data by userId. 

* ```coOccurrenceMatrix.java```: Build co-occurence matrix for each movies.

* ```Normalize.java``` and ```Normalize2.java```: These two mapreduce jobs were used to normalize the similarities. In each of them, the co-occurence matrix was divided by $ \sqrt{|N(i)|}$ and $ \sqrt{|N(j)|}$ respectively.

* ```Multiplication.java```: Use mapreduce matrix multiplication to multiply co-occurrce matrix and rating matrix.

* ```Sum.java```: Generating rating predictions for each pair of user and movie.

* ```RMSE.java```: This mapreduce job will compute square error for each prediction on test dataset.

* ```Result.java```: Collect all square errors and output the Root Mean Square Error

## Dependencies

* Ubuntu 16.04
* Python 2.7
* Java 1.7.0_111
* Hadoop 2.5.0
* docker 1.13.1

## How to Run
* Clone this respirtory and download the Netflix dataset into the ```data``` folder. 

* Start hadoop and enter this directory

* Run python ```data_preprocess.py -n 500 ``` to generate raw-input data for mapreduce.
You can choose the number of movies used by your preference and machine.

* Run ./run_hadoop.sh

## Result and Performance

The performance of this Recommender System has been evaluate on 100, 200 and 500 movies set now. Experiments will be implemented on larger dataset in the near future.


|#movies| RMSE|
|-----|-----| 
| 100| 1.1764|
|200 | 1.1901|
|500 | 1.0825|

