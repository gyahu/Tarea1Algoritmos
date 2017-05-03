#include <stdio.h>
#include <boost/filesystem.hpp>

using namespace std;

/* Moves the different values to the corresponding bucket */
void redirect(string batch, std::vector<int> pivots, std::vector<FILE *> buckets){

}

int main(){

	/* Global variables */
	int S = 0;
	int pivotAmmount = S*log2(S);
	size_t result;
	int size = sizeof(char);

	/* Iterate for the different possible data sizes */
	for (int i = 9; i < 22; ++i){

		/* Variables per data */
		std::vector<int> pivots(pivotAmmount, 0);
		std::vector<FILE *> buckets(pivotAmmount+1);

		/* Open the folder of the corresponding data */
		string dir_path("folder_2**"+to_string(i));
		boost::filesystem::path dir(dir_path);
		boost::filesystem::create_directory(dir);

		/* Open raw data */
		FILE * pFile;
  		fpos_t position;
		string buffer;
	  	pFile = fopen ("raw_data.txt", "r");
  		fgetpos (pFile, &position);
		buffer = (char *) malloc(size);

	  	/* SORT */

		int counter = 0;
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

	  	/* Restart the file */
	  	fsetpos (pFile, &position);

	  	/* Move data to buckets by batches */
	  	while (fread(buffer, 1, size, pFile)){
	  		redirect(buffer, pivots, buckets);
	  	}


	  	/* CLOSE */

	  	for (int i = 0; i < pivotAmmount; ++i){
	  		fclose(buckets[i]);
	  	}

	  	fclose(pFile)
	}
	return 0;
}