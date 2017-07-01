#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <malloc.h>
#include <dirent.h>
#include <string.h>

#define MAX_ETF_LENGTH 1537
#define CHARACTER_LENGTH 3
#define BUY_INDEX 0
#define SELL_INDEX 1
#define HOLD_INDEX 2
#define MAX_UNITS 1000
#define INIT_CASH 300.0f
#define INVESTMENT 100.0f
#define DIR_NAME "c:\\downloaded_data\\USD\\"
#define MAX_DAYS 8912
#define START "1993-01-29"
#define END "2017-06-22"
#define MODIFIER 0.01f

typedef struct _unit{		
	int ID;
	float *etfs_pref;
	float *character;
	float cash;
	int *portfolio;
} *unit;

float uniform_distribution(float M, float N) {
    return M + (rand() / ( RAND_MAX / (N-M) ) ) ;
}

int random_at_most(int max) {
	unsigned int
	// max <= RAND_MAX < ULONG_MAX, so this is okay.
	num_bins = (unsigned int) max + 1,
	num_rand = (unsigned int) RAND_MAX + 1,
	bin_size = num_rand / num_bins,
	defect   = num_rand % num_bins;

	int x;
	do {
		x = random();
	}
	// This is carefully written not to overflow
	while (num_rand - defect <= (unsigned int)x);

	// Truncated division is intentional
	return x/bin_size;
}

void init_unit(unit u,int id){
	u->cash = INIT_CASH;	
	u->ID = id;
	
	u->etfs_pref = malloc(MAX_ETF_LENGTH*sizeof(float));
	u->portfolio = malloc(MAX_ETF_LENGTH*sizeof(int));
	u->character = malloc(3*sizeof(float));
	
	for(int i = 0;i < MAX_ETF_LENGTH;i++){
		u->etfs_pref[i] = 0.5f;
		u->portfolio[i] = 0;
	}
	for(int i = 0;i < CHARACTER_LENGTH;i++){
		u->character[i] = uniform_distribution(0.0f,1.0f);
	}
	
}

void print_unit(unit u){
	printf("Unit:%d\n",u->ID);
	printf("[");
	for(int i = 0; i < CHARACTER_LENGTH;i++){
		printf("%f,",u->character[i]);
	}
	printf("]\n{");
	for(int i = 0;i<MAX_ETF_LENGTH;i++){
		printf("%f,",u->etfs_pref[i]);
	}
	printf("]\n");
	printf("Cash:%f\n",u->cash);
}

void do_buy_action(unit u, float *value){
	int etf_index = random_at_most(MAX_ETF_LENGTH);
	float prob = uniform_distribution(0.0f,1.0f);
	if(u->etfs_pref[etf_index] >= prob ){
		
		if(u->etfs_pref[etf_index]+MODIFIER < 1.0f){
			u->etfs_pref[etf_index]+=MODIFIER;
		}
		
		float val = value[etf_index];
		if((int)val == 0){
			return;
		}
		if(val > u->cash){
			return;
		}
		
		int shares = (int)(u->cash/val);		
		u->cash -= (float)(shares*val);				
		u->portfolio[etf_index] +=shares;
		
		printf("unit[%d], B, %d,%f, P[%d]=%d,Pref[%d]=%f\n",u->ID,shares,val,etf_index,u->portfolio[etf_index],etf_index,u->etfs_pref[etf_index]);
		
	}
}

void do_sell_action(unit u, float *value){
	int etf_index = random_at_most(MAX_ETF_LENGTH);
	if(u->portfolio[etf_index] == 0){
		return;
	}
	
	float prob = uniform_distribution(0.0f,1.0f);
	if(u->etfs_pref[etf_index] >= prob ){
		float val = value[etf_index];
		if((int)val == 0){
			return;
		}
		int shares = u->portfolio[etf_index];
		u->cash +=  (float)(shares*val);
		u->portfolio[etf_index] = 0;
		printf("unit[%d], S, %d,%f, P[%d]=%d,Pref[%d]=%f\n",u->ID,shares,val,etf_index,u->portfolio[etf_index],etf_index,u->etfs_pref[etf_index]);
	}
	
}

void oneStep(unit *population,float *values ){
	for(int i = 0;i < MAX_UNITS;i++){
		float choice = uniform_distribution(0.0f,1.0f);
		unit u = population[i];
		if(u->character[HOLD_INDEX] >= choice){
			continue;
		}
		if(u->character[BUY_INDEX] >= choice){
			do_buy_action(u, values);
		}
		if(u->character[SELL_INDEX] >= choice){
			do_sell_action(u,values);
		}		
	}
}



time_t get_time(char *time_details){
	struct tm tm;
	memset(&tm, 0, sizeof(struct tm));
	strptime(time_details, "%Y-%m-%d %H:%M:%S", &tm);
	return mktime(&tm);
}

void load_etf_values(float **values,char **etf_names){
	DIR *dir;
	struct dirent *ent;
	time_t start = get_time(START);
	
	if ((dir = opendir (DIR_NAME)) != NULL) {
	  /* print all the files and directories within directory */
		int etf_index = 0;
		while ((ent = readdir (dir)) != NULL) {
			if ( !strcmp(ent->d_name, ".") || !strcmp(ent->d_name, "..") ){
				continue;
			}
			
			char etf_name[strlen(ent->d_name)-3];
			memcpy(etf_name, ent->d_name, strlen(ent->d_name)-4);
			etf_name[strlen(ent->d_name)-4]='\0';
			
			etf_names[etf_index]=malloc(strlen(etf_name)*sizeof(char));
			memcpy(etf_names[etf_index], etf_name, strlen(etf_name)+1);
			printf("Loading ETF[%d]=%s\n",etf_index,etf_names[etf_index]);
			
			FILE *file;
			int filename_length = strlen(ent->d_name)+strlen(DIR_NAME)+1;
			char *filename = malloc(filename_length*sizeof(char));
			memset(filename,0,filename_length*sizeof(char));
			strncat(filename, DIR_NAME, strlen(DIR_NAME));
			strncat(filename,ent->d_name,strlen(ent->d_name));
			printf("Loading filename:%s\n",filename);
			
			
			file = fopen(filename,"r");
			if (file == NULL)
				exit(EXIT_FAILURE);
			
			char * line = NULL;
			size_t len = 0;
			ssize_t read;
			
			while ((read = getline(&line, &len, file)) != -1) {
					char *token = strtok(line, ",");
					int count = 0;
					int index = 0;
					while(token) {
						if(count == 0){
							//date
							time_t cur = get_time(token);
							index = (int)((cur-start)/86400);
							count++;
						}else{
							//float
							float val = atof(token);
							printf("values[%d][%d]=%f\n",index,etf_index,val);
							values[index][etf_index] = val;
							printf("Values setting done for values[%d][%d]=%f\n",index,etf_index,val);
						}
						token = strtok(NULL, " ");
					}
					
			}
			fclose(file);
			if(line){
				free(line);
			}
			//printf ("file processed: %s\n", filename);
			if(filename){
				free(filename);
			}
			etf_index++;
		}
	  
		closedir (dir);
	} else {
	  /* could not open directory */
	  perror ("");
	  exit(EXIT_FAILURE);
	}
}

float compute_value(int day,unit u, float **values){
	float sum = u->cash;
	for(int i=0;i < MAX_ETF_LENGTH;i++){
		if(u->portfolio[i] == 0)
			continue;
		int days_iterator = day;
		float val = values[day][i];
		while((int)val == 0 && days_iterator > 0){
			val = values[days_iterator--][i];			
		}
		int shares = u->portfolio[i];
		float ssum=(float)(val*shares);
		printf("Summing Unit[%d]=%f*%d=%f\n",u->ID,val,shares,ssum);
		sum+=ssum;
	}
	
	return sum;
}


int main() 
{

	unit *population = malloc(MAX_UNITS*sizeof(unit));
	float **values = malloc(MAX_DAYS*sizeof(float));
	char **etf_names = malloc(MAX_ETF_LENGTH*sizeof(char));
	
	srand((unsigned) time(NULL));
	for(int i = 0;i < MAX_UNITS;i++){
		population[i] = malloc(sizeof(struct _unit));
		init_unit(population[i],i);
		printf("Create unit[%d] with ID=%d\n",i,population[i]->ID);
	}	
	for(int i = 0;i < MAX_DAYS;i++){
		values[i] = malloc(MAX_ETF_LENGTH*sizeof(float));
	}
	for(int i = 0; i < MAX_DAYS;i++){
		for(int j = 0;j < MAX_ETF_LENGTH;j++){
			values[i][j]=0.0f;
		}
	}
	
	//rozdiel dvoch datumov je 86400 ak je to den
	load_etf_values(values,etf_names);
	
	for(int i = 0; i < MAX_DAYS;i++){
		printf("day:%d\n",i);
		if(i % 90 ==0){
			for(int j =0;j < MAX_UNITS;j++){
				population[j]->cash += INVESTMENT;
			}
		}
		oneStep(population,values[i]);
	}
	
	float maxSum = 0.0f;
	int id_unit = 0;
	for(int i= 1;i < MAX_UNITS;i++){
		unit u = population[i];
		printf("Ready to sum unit[%d] with ID=%d\n",i,u->ID);
		float sum = compute_value(MAX_DAYS-1,u,values);
		if(sum > maxSum){
			maxSum=sum;
			id_unit = u->ID;
		}
	}
	
	printf("winner[%d]=%f\n",id_unit,maxSum);
	print_unit(population[id_unit]);
	
	for(int i = 0;i < MAX_UNITS;i++) {
		free(population[i]);
	}
	free(population);
	
	for(int i = 0;i < MAX_DAYS;i++){
		free(values[i]);
	}
	free(values);
	
	for(int i = 0;i < MAX_ETF_LENGTH;i++){
		free(etf_names[i]);
	}
	free(etf_names);
	
	return 0;
}
