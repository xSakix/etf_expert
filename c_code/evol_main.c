#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <malloc.h>
#include <dirent.h>
#include <string.h>
#include <limits.h>
#include <sys/stat.h>

#define MAX_ETF_LENGTH 1414
#define CHARACTER_LENGTH 4
#define BUY_INDEX 0
#define SELL_INDEX 1
#define HOLD_INDEX 2
#define ANIMAL_SPIRIT_INDEX 3
#define MAX_UNITS 1000
//#define MAX_UNITS 1000
#define INIT_CASH 300.0f
#define INVESTMENT 300.0f
#define DIR_NAME "c:\\downloaded_data\\USD\\"
#define MAX_DAYS 9000
#define START "1993-01-29"
#define END "2017-06-22"
#define MODIFIER 0.01f
#define MAX_ITER 100

typedef struct _unit {
	int ID;
	float *etfs_pref;
	float *character;
	float cash;
	int *portfolio;
} *unit;

float uniform_distribution(float M, float N) {
	return M + (rand() / (RAND_MAX / (N - M)));
}

int random_at_most(int max) {
	unsigned int
		// max <= RAND_MAX < ULONG_MAX, so this is okay.
		num_bins = (unsigned int)max + 1,
		num_rand = (unsigned int)RAND_MAX + 1,
		bin_size = num_rand / num_bins,
		defect = num_rand % num_bins;

	int x;
	do {
		x = random();
	}
	// This is carefully written not to overflow
	while (num_rand - defect <= (unsigned int)x);

	// Truncated division is intentional
	return x / bin_size;
}

void init_unit(unit u, int id) {
	u->cash = INIT_CASH;
	u->ID = id;

	u->etfs_pref = malloc(MAX_ETF_LENGTH * sizeof(float));
	u->portfolio = malloc(MAX_ETF_LENGTH * sizeof(int));
	u->character = malloc(3 * sizeof(float));

	for (int i = 0; i < MAX_ETF_LENGTH; i++) {
		u->etfs_pref[i] = 0.5f;
		u->portfolio[i] = 0;
	}
	for (int i = 0; i < CHARACTER_LENGTH; i++) {
		u->character[i] = uniform_distribution(0.0f, 0.99f);
	}
	u->character[ANIMAL_SPIRIT_INDEX] = uniform_distribution(0.0f, 0.1f);
}

static void _mkdir(const char *dir) {
	char tmp[PATH_MAX];
	char *p = NULL;
	size_t len;

	snprintf(tmp, sizeof(tmp), "%s", dir);
	len = strlen(tmp);
	if (tmp[len - 1] == '/')
		tmp[len - 1] = 0;
	for (p = tmp + 1; *p; p++)
		if (*p == '/') {
			*p = 0;
			mkdir(tmp, S_IRWXU);
			*p = '/';
		}
	mkdir(tmp, S_IRWXU);
}

void log_unit(unit u, char **etf_names, char *dirpath) {

	//FILE *fopen( const char * filename, const char * mode );

	char log_file[PATH_MAX];
	sprintf(log_file, "%s/%d_history.log", dirpath, u->ID);
	FILE *log = fopen(log_file, "a+");

	if (log == NULL)
		exit(EXIT_FAILURE);

	//fputs(const char *s, FILE *fp);

	char buffer[80];
	sprintf(buffer, "Unit:%d\n[", u->ID);
	fputs(buffer, log);

	for (int i = 0; i < CHARACTER_LENGTH; i++) {
		memset(buffer, 0, strlen(buffer));
		sprintf(buffer, "%f,", u->character[i]);
		fputs(buffer, log);
	}

	memset(buffer, 0, strlen(buffer));
	sprintf(buffer, "]\n{");
	fputs(buffer, log);

	for (int i = 0; i < MAX_ETF_LENGTH; i++) {
		memset(buffer, 0, strlen(buffer));
		sprintf(buffer, "%s=%.2f,", etf_names[i], u->etfs_pref[i]);
		fputs(buffer, log);
	}

	memset(buffer, 0, strlen(buffer));
	sprintf(buffer, "},{");
	fputs(buffer, log);

	for (int i = 0; i < MAX_ETF_LENGTH; i++) {
		memset(buffer, 0, strlen(buffer));
		sprintf(buffer, "%s=%d,", etf_names[i], u->portfolio[i]);
		fputs(buffer, log);
	}

	memset(buffer, 0, strlen(buffer));
	sprintf(buffer, "}\nCash:%.2f\n", u->cash);
	fputs(buffer, log);

	fclose(log);
}

float value_of_etf(int day, int etf_index, float **values) {

	int days_iterator = day;

	float val = values[day][etf_index];

	while ((int)val == 0 && days_iterator > 0) {
		val = values[days_iterator--][etf_index];
	}

	return val;
}

void do_buy_action(int day, unit u, float **values) {
	int etf_index = random_at_most(MAX_ETF_LENGTH);
	float prob = uniform_distribution(0.0f, 0.99f);
	if (u->etfs_pref[etf_index] >= prob) {

		float calc = (float)u->etfs_pref[etf_index] + MODIFIER;
		if (calc < 0.99f) {
			u->etfs_pref[etf_index] = calc;
		}

		float val = value_of_etf(day, etf_index, values);
		if ((int)val == 0) {
			return;
		}
		if (val > u->cash) {
			return;
		}

		int shares = (int)(u->cash / val);
		u->cash -= (float)(shares*val);
		u->portfolio[etf_index] += shares;

		//printf("unit[%d], B, %d,%f, P[%d]=%d,Pref[%d]=%f\n",u->ID,shares,val,etf_index,u->portfolio[etf_index],etf_index,u->etfs_pref[etf_index]);

	}
}

void do_sell_action(int day, unit u, float **values) {
	int etf_index = random_at_most(MAX_ETF_LENGTH);
	if (u->portfolio[etf_index] == 0) {
		return;
	}

	float prob = uniform_distribution(0.0f, 0.99f);
	if (u->etfs_pref[etf_index] >= prob) {
		float val = value_of_etf(day, etf_index, values);
		if ((int)val == 0) {
			return;
		}
		int shares = u->portfolio[etf_index];
		u->cash += (float)(shares*val);
		u->portfolio[etf_index] = 0;
		//printf("unit[%d], S, %d,%f, P[%d]=%d,Pref[%d]=%f\n",u->ID,shares,val,etf_index,u->portfolio[etf_index],etf_index,u->etfs_pref[etf_index]);
	}

}

void sell_all_shares(int day, unit u, float **values) {

	for (int i = 0; i < MAX_ETF_LENGTH; i++) {
		if (u->portfolio[i] > 0) {
			float val = value_of_etf(day, i, values);
			int shares = u->portfolio[i];
			u->cash += (float)(shares*val);
			u->portfolio[i] = 0;
		}
	}
}

int best_index_present(int best_index, int *best)
{
	for (int i = 0; i < 10; i++) {
		if (best[i] == best_index)
			return 1;
	}

	return 0;

}

void get_ten_best(unit u, int *best) {
	memset(best, -1, 10);

	int index = 0;

	while (index < 10)
	{

		int best_index = 0;
		while (best_index_present(best_index, best)) {
			best_index++;
		}
		float prefs = 0.5f;

		for (int i = 0; i < MAX_ETF_LENGTH; i++) {
			if (u->etfs_pref[i] > prefs && !best_index_present(i, best)) {
				prefs = u->etfs_pref[i];
				best_index = i;
			}
		}
		//printf("Unit[%d] found etf[%d]\n", u->ID, best_index);
		best[index] = best_index;
		index++;
	}
}

void buy_animal_action_from_ten_best(int day, unit u, float **values) {

	int* best = malloc(10 * sizeof(int));
	//printf("Unit[%d] looking for best etf\n", u->ID);
	get_ten_best(u, best);
	//printf("Unit[%d] found best etf\n", u->ID);

	for (int i = 0; i < 10; i++) {

		int etf_index = best[i];
		if (etf_index == -1)
		{
			continue;
		}


		float val = value_of_etf(day, etf_index, values);
		if ((int)val == 0) {
			continue;
		}
		if (val > u->cash) {
			continue;
		}

		int shares = (int)(u->cash / val);
		u->cash -= (float)(shares*val);
		u->portfolio[etf_index] += shares;
	}
	//printf("Unit[%d] animal spirit buy inner nearly DONE.\n", u->ID);
	free(best);
	//printf("Unit[%d] animal spirit buy inner DONE.\n", u->ID);
}


void do_buy_action_animal(int day, unit u, float **values) {
	int etf_index = random_at_most(MAX_ETF_LENGTH);
	float calc = (float)u->etfs_pref[etf_index] + MODIFIER;
	if (calc < 0.99f) {
		u->etfs_pref[etf_index] = calc;
	}

	float val = value_of_etf(day, etf_index, values);
	if ((int)val == 0) {
		return;
	}
	if (val > u->cash) {
		return;
	}

	int shares = (int)(u->cash / val);
	u->cash -= (float)(shares*val);
	u->portfolio[etf_index] += shares;


}

void oneStep(int day, unit *population, float **values) {


	for (int i = 0; i < MAX_UNITS; i++) {
		float choice = uniform_distribution(0.0f, 0.99f);
		unit u = population[i];

		if (u->character[ANIMAL_SPIRIT_INDEX] >= choice) {
			sell_all_shares(day, u, values);
			do_buy_action_animal(day, u, values);

			continue;
		}

		if (u->character[HOLD_INDEX] >= choice) {
			continue;
		}
		if (u->character[BUY_INDEX] >= choice) {
			do_buy_action(day, u, values);
		}
		if (u->character[SELL_INDEX] >= choice) {
			do_sell_action(day, u, values);
		}
	}
}



time_t get_time(char *time_details) {
	struct tm tm;
	memset(&tm, 0, sizeof(struct tm));
	strptime(time_details, "%Y-%m-%d %H:%M:%S", &tm);
	return mktime(&tm);
}

float put_etf_value(int day, int etf_index, float **values) 
{
	//hladam hodnotu
	//najskor ju hladam v minulosti
	while (day >= 0) {
		float val = values[day--][etf_index];
		if ((int)val > 0) {
			return val;
		}
	}
	//ak ju nenajdem, hladam v buducnosti
	while (day < MAX_DAYS) {
		float val = values[day++][etf_index];
		if ((int)val > 0) {
			return val;
		}
	}

}

void normalize_etf_values(float **values) 
{
	for (int day = 0; day < MAX_DAYS; day++) {
		for (int etf_index = 0; etf_index < MAX_ETF_LENGTH; etf_index++) {
			if ((int)values[day][etf_index] == 0) {
				values[day][etf_index] = put_etf_value(day, etf_index, values);
			}
		}
	}
}

void load_etf_values(float **values, char **etf_names) {
	printf("[START]Loading ETF values...\n");

	DIR *dir;
	struct dirent *ent;
	time_t start = get_time(START);

	if ((dir = opendir(DIR_NAME)) != NULL) {
		/* print all the files and directories within directory */
		int etf_index = 0;
		while ((ent = readdir(dir)) != NULL) {
			if (!strcmp(ent->d_name, ".") || !strcmp(ent->d_name, "..")) {
				continue;
			}

			char etf_name[strlen(ent->d_name) - 3];
			memcpy(etf_name, ent->d_name, strlen(ent->d_name) - 4);
			etf_name[strlen(ent->d_name) - 4] = '\0';

			etf_names[etf_index] = malloc(strlen(etf_name) * sizeof(char));
			memcpy(etf_names[etf_index], etf_name, strlen(etf_name) + 1);
			//printf("Loading ETF[%d]=%s\n",etf_index,etf_names[etf_index]);

			FILE *file;
			int filename_length = strlen(ent->d_name) + strlen(DIR_NAME) + 1;
			char *filename = malloc(filename_length * sizeof(char));
			memset(filename, 0, filename_length * sizeof(char));
			strncat(filename, DIR_NAME, strlen(DIR_NAME));
			strncat(filename, ent->d_name, strlen(ent->d_name));
			//printf("Loading filename:%s\n",filename);


			file = fopen(filename, "r");
			if (file == NULL)
				exit(EXIT_FAILURE);

			char * line = NULL;
			size_t len = 0;
			ssize_t read;

			while ((read = getline(&line, &len, file)) != -1) {
				char *token = strtok(line, ",");
				int count = 0;
				int index = 0;
				while (token) {
					if (count == 0) {
						//date
						time_t cur = get_time(token);
						index = (int)((cur - start) / 86400);
						count++;
					}
					else {
						//float
						float val = atof(token);
						//printf("values[%d][%d]=%f\n",index,etf_index,val);
						values[index][etf_index] = val;
						//printf("Values setting done for values[%d][%d]=%f\n",index,etf_index,val);
					}
					token = strtok(NULL, " ");
				}

			}
			fclose(file);
			if (line) {
				free(line);
			}
			//printf ("file processed: %s\n", filename);
			if (filename) {
				free(filename);
			}
			etf_index++;
		}

		closedir(dir);
	}
	else {
		/* could not open directory */
		perror("");
		exit(EXIT_FAILURE);
	}

	printf("[START]Normalizing ETF values...\n");
	normalize_etf_values(values);
	printf("[DONE]Normalizing ETF values...\n");

	printf("[DONE]Loading ETF values...\n");
}

float compute_value(int day, unit u, float **values) {
	float sum = u->cash;
	for (int i = 0; i < MAX_ETF_LENGTH; i++) {
		if (u->portfolio[i] == 0)
			continue;

		float val = value_of_etf(day, i, values);
		int shares = u->portfolio[i];
		float ssum = (float)(val*shares);
		sum += ssum;
	}

	return sum;
}

void get_time_string(char *buffer) {
	time_t rawtime;
	struct tm *info;

	time(&rawtime);

	info = localtime(&rawtime);

	strftime(buffer, 80, "%Y%m%d%H%M%s", info);
}

int main()
{
	printf("STARTING....\n");


	unit *population = malloc(MAX_UNITS * sizeof(unit));
	float **values = malloc(MAX_DAYS * sizeof(float*));
	char **etf_names = malloc(MAX_ETF_LENGTH * sizeof(char*));

	srand((unsigned)time(NULL));
	for (int i = 0; i < MAX_UNITS; i++) {
		population[i] = malloc(sizeof(struct _unit));
		init_unit(population[i], i);
	}
	for (int i = 0; i < MAX_DAYS; i++) {
		values[i] = malloc(MAX_ETF_LENGTH * sizeof(float));
	}
	for (int i = 0; i < MAX_DAYS; i++) {
		for (int j = 0; j < MAX_ETF_LENGTH; j++) {
			values[i][j] = 0.0f;
		}
	}

	//rozdiel dvoch datumov je 86400 ak je to den
	load_etf_values(values, etf_names);

	char datetime_part[80];
	get_time_string(datetime_part);

	for (int iter = 0; iter < MAX_ITER; iter++)
	{
		char dir_path[PATH_MAX];

		sprintf(dir_path, "c:/DATA/etf_evolution/%s/%d_Cevotion", datetime_part, iter);
		_mkdir(dir_path);

		printf("Starting iteration[%d]\n", iter);
		for (int i = 0; i < MAX_DAYS; i++) {
			if (i % 30 == 0) {
				for (int j = 0; j < MAX_UNITS; j++) {
					population[j]->cash += INVESTMENT;
				}
			}
			printf("Starting DAY[%d][%d]\n", iter,i);
			oneStep(i, population, values);
		}

		float maxSum = 0.0f;
		int id_unit = 0;
		for (int i = 1; i < MAX_UNITS; i++) {
			unit u = population[i];
			float sum = compute_value(MAX_DAYS - 1, u, values);
			if (sum > maxSum) {
				maxSum = sum;
				id_unit = u->ID;
			}
		}

		printf("winner[%d]=%.2f\n", id_unit, maxSum);

		char winner_dir[PATH_MAX];
		sprintf(winner_dir, "%s/%.2f", dir_path, maxSum);
		_mkdir(winner_dir);

		log_unit(population[id_unit], etf_names, winner_dir);

		int count9 = 0;
		for (int i = 0; i < MAX_ETF_LENGTH; i++)
		{
			if (population[id_unit]->etfs_pref[i] >= 0.9f) {
				count9++;
			}
		}

		if (count9 > 9) {
			break;
		}

		for (int i = 0; i < MAX_UNITS; i++)
		{
			unit u = population[i];
			for (int j = 0; j < MAX_ETF_LENGTH; j++) {
				u->portfolio[j] = 0;
			}
			u->cash = INIT_CASH;
		}

	}

	for (int i = 0; i < MAX_UNITS; i++) {
		free(population[i]);
	}
	free(population);

	for (int i = 0; i < MAX_DAYS; i++) {
		free(values[i]);
	}
	free(values);

	for (int i = 0; i < MAX_ETF_LENGTH; i++) {
		free(etf_names[i]);
	}
	free(etf_names);

	printf("END....\n");


	return 0;
}
