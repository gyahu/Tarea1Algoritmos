#include <stdio.h>
#include <vector.h>
#include <boost/filesystem.hpp>

using namespace std;

/* Moves the different values to the corresponding bucket */
void redirect(string batch, std::vector<float> pivots, std::vector<FILE *> buckets, int index){
	std::istringstream batch(batch);
	std::vector<strings> coordinates;

	/* Process the batch by line */
    while (std::getline(batch, line)) {
    	boost::split(coordinates, line, boost::is_any_of(" "));
        float primary = strtof(coordinates[index], NULL);
        unsigned int i = 0;
        for (; i < pivots.size(); ++i){
        	if (pivots[i] > primary){
        		buckets[i] << line << "\n";
        		break;
        	}
        }
        if pivots[i-1] < primary{
        	buckets[i] << line << "\n";
        }
    }
}

struct Comparator {
    Comparator(int index) { this->index = index; }
    bool operator(std::vector<string> coordinates1, std::vector<string> coordinates2){
		float primary1 = strtof(coordinates1[index], NULL);
		float secondary1 = strtof(coordinates1[index+2], NULL);
	    float tertiary1 = strtof(coordinates1[(index+1)%2], NULL);
	    float quaternary1 = strtof(coordinates1[(index+1)%2+2], NULL);
		float primary2 = strtof(coordinates1[index], NULL);
		float secondary2 = strtof(coordinates1[index+2], NULL);
	    float tertiary2 = strtof(coordinates1[(index+1)%2], NULL);
	    float quaternary2 = strtof(coordinates1[(index+1)%2+2], NULL);
	    if (primary1 == primary2){
	    	if (secondary1 == secondary2){
	    		if (tertiary1 == tertiary2){
	    			return quaternary1 < quaternary2;
	    		}
	    		return tertiary1 < tertiary2;
	    	}
	    	return secondary1 < secondary2;
	    }
	    return primary1 <primary2;
	}
    int index;
};

void mainSort(string batch, int index){
	std::istringstream batch(batch);
	std::vector<strings> coordinates;
	std::vector<std::vector<float>> segments;
	while (std::getline(batch, line)) {
		boost::split(coordinates, line, boost::is_any_of(" "));
        segments.push_back(coordinates);
    }
    std::sort(segments.begin(), segments.end()), Comparator(index));
}

/* Sorts the file using pivotAmmount pivots and acording to the index */
void fileSort(FILE * data, int pivotAmmount, int index, int mainMemory){

	size_t result;
	string buffer;
	buffer = (char *) malloc(size);

	/* Check size file againt mainMemory */
	file.seekg(0, ios::end);
    int fileSize = file.tellg();

    if (fileSize <= mainMemory){
    	result = fread(buffer, 1, size, data);
    	mainSort(buffer, index);
    }
	else{
		/* Variables per sort */
		std::vector<float> pivots(pivotAmmount, 0);
		std::vector<FILE *> buckets(pivotAmmount+1, NULL);
		
	  	fpos_t position;
		int counter = 0;

	  	fgetpos (data, &position);

	  	/* Get pivots */
	  	while (counter < pivotAmmount){

		  	/* Move data to main memory */
		  	result = fread(buffer, 1, size, data);

		  	{
		  	// pivots[counter] = value;

		  	++counter;
		  	}
	  	}

	  	/* Open files for each bucket */
	  	for (int i = 0; i < pivotAmmount; ++i){
	  		buckets[i] = fopen("bucket"+to_string(i), "a+"); //Recursion problem, specify the name acording to data variable.
	  	}

	  	/* Restart the file position */
	  	fsetpos (data, &position);

	  	/* Move data to buckets by batches */
	  	while (fread(buffer, 1, size, data)){
	  		redirect(buffer, pivots, buckets, index);
	  	}

	  	for (int i = 0; i < pivotAmmount; ++i){
	  		fileSort(buckets[i], pivotAmmount, index, mainMemory);
	  	}

	  	/* CLOSE */
	  	for (int i = 0; i < pivotAmmount; ++i){
	  		fclose(buckets[i]);
	  	}
    }
}

int main(){

	/* Global variables */
	int M = 0;
	int S = 0;
	int pivotAmmount = S*log2(S);
	int size = sizeof(char);

	/* Iterate for the different possible data sizes */
	for (int i = 9; i < 22; ++i){
		FILE * pFile;

		/* Open raw data */
		string dir_path("folder_2**"+to_string(i));
	  	pFile = fopen (dir_path+"raw_data.txt", "r");

		fileSort(pFile, pivotAmmount, 0, M);
		fileSort(pFile, pivotAmmount, 1, M);
			
	  	/* CLOSE */
	  	fclose(pFile)
	}
	return 0;
}