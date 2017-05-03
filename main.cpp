#include <stdio.h>
#include <boost/filesystem.hpp>

using namespace std;

/* Moves the different values to the corresponding bucket */
void redirect(string batch, std::vector<int> pivots, std::vector<FILE *> buckets, int index){

}

/* Sorts the file using pivotAmmount pivots and acording to the index */
void fileSort(FILE * data, int pivotAmmount, int index, int mainMemory){

	/* Check size file againt mainMemory */
	file.seekg(0, ios::end);
    int fileSize = file.tellg();

    if (fileSize <= mainMemory){
    	// standard sorting algorithm
    }
	else{

		/* Variables per sort */
		std::vector<int> pivots(pivotAmmount, 0);
		std::vector<FILE *> buckets(pivotAmmount+1);
		FILE * output;
		string buffer;
	  	fpos_t position;
		int counter = 0;

		buffer = (char *) malloc(size);
	  	fgetpos (pFile, &position);

	  	/* Get pivots */
	  	while (counter < pivotAmmount){

		  	/* Move data to main memory */
		  	result = fread(buffer, 1, size, pFile);

		  	{
		  	// pivots[counter] = value;

		  	++counter;
		  	}
	  	}

	  	/* Open files for each bucket */
	  	for (int i = 0; i < pivotAmmount; ++i){
	  		buckets[i] = fopen("bucket"+to_string(i), "a+");
	  	}

	  	/* Restart the file position */
	  	fsetpos (pFile, &position);

	  	/* Move data to buckets by batches */
	  	while (fread(buffer, 1, size, pFile)){
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
	size_t result;
	int size = sizeof(char);

	/* Iterate for the different possible data sizes */
	for (int i = 9; i < 22; ++i){
		FILE * pFile;

		/* Open raw data */
		string dir_path("folder_2**"+to_string(i));
	  	pFile = fopen (dir_path+"raw_data.txt", "r");

		fileSort(pFile, pivotAmmount, 1, M);
		fileSort(pFile, pivotAmmount, 2, M);
			
	  	/* CLOSE */
	  	fclose(pFile)
	}
	return 0;
}