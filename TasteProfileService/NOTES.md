Distributed systems assignment 1:

	Data types: (William)
		-> UserCounter
		-> TopThreeUsers
		-> SongCounter
		-> TopThreeSongs

		-> SongProfile, for server side caching
		-> UserProfile, for server and client side caching

	Server: (William)
	   -> getTimesPlayed
	   -> getTimesPlayedByUser
	   -> getTopThreeUsersBySong
	   -> getTopThreeSongsByUser

	   -> getUserProfile, for client side caching


	Client: (Alin)
		-> reads the input file
		-> invokes the correct methods
		-> writes "statistics" to specific output files

Notes: 

	We could build the caches at server startup time, before the request
		-> both the SongProfileCache and the UserProfileCache

Timeline:
	1. Create the idlj schema for the data types and the methods
	2. Each one implements their bits