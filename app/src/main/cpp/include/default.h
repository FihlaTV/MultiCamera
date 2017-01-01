#ifndef __DEFAULT__
#define __DEFAULT__

#define __DEBUG_MODE__
#define STRINGIFY(A)  #A

#include "logger.h"

#define 	TRUE 	1
#define 	FALSE	0

typedef struct __Rect {
	int 	x;
	int 	y;
	int 	width;
	int 	height;
} Rect;

#endif
