import numpy as np
import random
import pandas as pd
import math

alpha = [0.25, 0.5, 0.75]
N = list(range(9,22))
distributions = ['uniform', 'normal']
file_names = []

def generate_files():
    for n in N:
        for a in alpha:
            for d in distributions:
                f_name = 'input-2-{}-{}-{}.csv'.format(str(n),d,a)
                segments = generate_segments(n, d, a)
                df = pd.DataFrame(segments)
                df.to_csv(f_name, index=False, header=False)
                print("Finished generating input of size 2^{}, alpha = {} and distribution = {}\n".format(n,a,d))

def generate_segments(N, distribution, alpha):
    vs_qty = int((2**N)*(1-alpha))
    hs_qty = int((2**N)*alpha)


    segments = []
    bound = math.floor(2**(N/2))
    for i in range(0,vs_qty):
        ## --------------------------------------------------------------------
        ## --------------------------------------------------------------------
        ## First we generate x coordinates with its corresponding distribution:

        if(distribution == 'uniform'):
            x_1 = x_2 = math.floor(np.random.uniform(-bound, bound))
        elif(distribution == 'normal'):
            ## 1.0 : The idea here is to have 95% of the points between -2^(N/2) and 2^(N/2) .
            ##       Still not sure if it will behave well, we could need to change this.
            sigma = bound / 1.65
            x_1 = x_2 = math.floor(np.random.normal(0, sigma))

        ## --------------------------------------------------------------------
        ## --------------------------------------------------------------------

        ## Now we generate the y coordinates:
        ## 1.1 : Not sure if we need to check that y_1 != y_2, could this really not happen?
        y_1 = math.floor(np.random.uniform(-bound, bound))
        y_2 = math.floor(np.random.uniform(-bound, bound))
        while(y_1 == y_2):
            y_1 = math.floor(np.random.uniform(-bound, bound))
            y_2 = math.floor(np.random.uniform(-bound, bound))
        ## --------------------------------------------------------------------
        segments.append([x_1, y_1, x_2, y_2])

    for i in range(0,hs_qty):
        ## 1.2 : IDEM 1.1
        x_1 = math.floor(np.random.uniform(-bound, bound))
        x_2 = math.floor(np.random.uniform(-bound, bound))
        while(x_1 == x_2):
            x_1 = math.floor(np.random.uniform(-bound, bound))
            x_2 = math.floor(np.random.uniform(-bound, bound))

        y_1 = y_2 = math.floor(np.random.uniform(-bound, bound))
        segments.append([x_1, y_1, x_2, y_2])


    ## 1.3 : If we don't shuffle, then the segments list would end up with all the
    ##       vertical segments first and then all the horizontal ones.
    np.random.shuffle(segments)
    return segments

generate_files()
