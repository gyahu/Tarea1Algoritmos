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
		string buffer;
	  	pFile = fopen ("raw_data.txt", "w");

	  	/* SORT */
	  	/* Get pivots */
	  	for (int i = 0; i < pivotAmmount; ++i){

		  	/* Move data to main memory */
		  	buffer = (char *) malloc(size);
		  	result = fread(buffer, 1, size, pFile);

		  	// pivots[i] = value;

	  	}


	  	fclose(pFile)
	}
	return 0;
}