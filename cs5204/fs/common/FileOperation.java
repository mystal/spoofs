package cs5204.fs.common;

public enum FileOperation
{
	NO_OP, 
	CREATE, 
	MKDIR, 
	OPEN, 
	CLOSE,
	READ, 
	WRITE, 
	APPEND, 
	REMOVE,
	RMDIR,
	GET_ATTRIB
}