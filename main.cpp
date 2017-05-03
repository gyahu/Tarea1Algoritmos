#include <stdio.h>
#include <boost/filesystem.hpp>

using namespace std;

/* Moves the different values to the corresponding bucket */
void redirect(string batch, std::vector<int> pivots, std::vector<FILE *> buckets, int index){

}

/* Sorts the file using pivotAmmount pivots and acording to the index */
void sort(FILE * data, int pivotAmmount, int index){

	/* Variables per sort */
	std::vector<int> pivots(pivotAmmount, 0);
	std::vector<FILE *> buckets(pivotAmmount+1);
	string buffer;
  	fpos_t position;
	int counter = 0;

	buffer = (char *) malloc(size);
  	fgetpos (pFile, &position);

  	/* Get pivots */
  	while (counter < pivotAmmount){

	  	/* Move data to main memory */
	  	result = fread(buffer, 1, size, pFile);

	  	// pivots[counter] = value;

	  	++counter;
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

  	/* CLOSE */
  	for (int i = 0; i < pivotAmmount; ++i){
  		fclose(buckets[i]);
  	}
}

int main(){

	/* Global variables */
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

		sort(pFile, pivotAmmount, 1);
		sort(pFile, pivotAmmount, 2);
			
	  	/* CLOSE */
	  	fclose(pFile)
	}
	return 0;
}