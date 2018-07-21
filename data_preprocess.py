import os
import random
from optparse import OptionParser


def preprocess_data(input_dir, n):
    '''generator raw-input data for mapreduce job
    this function will select the first n movies from the entire
    netflix movie dataset and write them in 
    userId,movieId,rating format'''
    file_list = os.listdir(input_dir)[:n]
    i = 1
    user_movie = dict() # dict to store user:movie,rating mapping

    for file in file_list:
        fname = os.path.join(input_dir, file)
        if not os.path.exists(fname):
            raise Exception("Miss original input file "+file)
        f = open(fname, 'r')
        while True:
            line = f.readline()
            if not line:
                break
            user_rating = line.split(",")
            if len(user_rating) < 3:
                continue
            user, rating = user_rating[0], user_rating[1]
            if user not in user_movie:
                user_movie[user] = [str(i) + "," + rating]
            else:
                user_movie[user].append(str(i) + "," + rating)
        i += 1
    return user_movie

def train_test_split(user_movie):
    if not os.path.exists("input"):
        os.mkdir("input")
    if not os.path.exists("test"):
        os.mkdir("test")

    f_train = open("input/movie_train_set.txt", 'w')
    f_test = open("test/movie_test_set.txt", 'w')
    for user in user_movie:
        items = user_movie[user]
        random.shuffle(items)
        split = int(len(items) * 0.8)
        for item in items[:split]:
            movie = item.split(",")[0]
            f_train.write(user + "," + item + '\n')

        for item in items[split:]:
            movie = item.split(",")[0]
            f_test.write(user + "," + item + '\n')
            movie = item.split(",")[0]

    f_train.close()
    f_test.close()
    print("Finished generating train and test raw-input")

if __name__ == '__main__':
    input_dir = "data/training_set"
    if not os.path.exists(input_dir):
        raise Exception("Miss input directory "+input_dir)
    parser = OptionParser()

    parser.add_option('-n', '--num of movies', dest='n', default=500)
    options, args = parser.parse_args()
   
    n = int(options.n)
    user_movie = preprocess_data(input_dir, n)
    train_test_split(user_movie)



