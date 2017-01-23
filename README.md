Constraints:
KABANA Log loading range: 99% ~ 1 minute, 100% < 5mins

Steps to Build:
	1. Create a new Git hub repository under CPP called log-search
	2. Create a new Jenkins job to run this in any environment
	3. Parameterise the following variables (via properties file provided via Jenkins)
			Kabana URL
			Start time	 (check logs from)
			End time (check logs to)
			List the keywords to search for (username, password, JDBC URLs IP addresses)
			URL,	 IP, password (Supplied from the test cases),username (Supplied from the test cases)
	4.  Create a rest based module to perform elastic search
	5.  Create a component to read from property resource file
	6.  Perform KABANA search based on input data
	7.  Test assertions to report failures

Steps to Run:
1. Start any tests job manually via jenkins
2. Schedule log secured verification job (as a downstream) to follow
every 5 after any run of Test