#### Purpose:
Demonstrate simple Client and Server communication using `SocketServer` and `Socket`classes.

Here a simple protocol is defined which uses protobuf.
The server sets up a game of memory that can be played by one or more clients.

See PROTOCOL.md for explanation of protocol.

To see the proto file see: src/main/proto which is the default location for proto files. 

Gradle is already setup to compile the proto files. 

### The protocol
See the PROTOCOL.md for details.


### How to run it (optional)
The proto file can be compiled using

``gradle generateProto``

This will also be done when building the project. 

You should see the compiled proto file in Java under build/generated/source/proto/main/java/buffers

Now you can run the client and server 

#### Default 
Server is Java
Per default on 9099
runServer

You have one example client in Java using the Protobuf protocol

Clients runs per default on 
host localhost, port 9099
Run Java:
	runClient


#### With parameters:
Java
gradle runClient -Pport=9099 -Phost='localhost'
gradle runServer -Pport=9099

### Constraints:
- [x] The project needs to run through Gradle (nothing really to do here, just keep the
   Gradle file as is)
- [x] (6 points) You need to implement the given protocol (Protobuf) see the
   Protobuf files, the README and the Protobuf example in the 'examples
   repo' (this is NOT an optional requirement). If you do not do this you will
   lose 15 points (instead of getting the 7 points) since then our client will not run
   your server for testing and thus basically your interface is wrong and not what the
   customer asked for.
- [x] (4 points) The main menu gives the user 3 options: 1: leaderboard, 2
   play game, 3 quit. After a game is done the menu should pop up again.
   Implement the menu on the Client side (not optional). So the client shows
   the menu, the Server does not send the menu options to the Client.
- [x] (3 points) When the user chooses the option 1, a leader board will be shown (does
   not have to be sorted or pretty).
- [x] (2.5 points) The leader board is the same for all users; take care that multiple users
   do not corrupt it.
- [x] (2.5 points) The leader board persists even if the server crashes and is re-started.
- [x] (2 points) User chooses option 2 (play game) and the current game board is shown.
- [x] (5 points) Multiple users will enter the same game and will thus play the game faster.
   NOTE: The server will only run a single instance of the game at any given time.
   If a user requests a new game and a game is in progress, they will join the current
   game. See video for details.
- [x] (2 points) Users win after finding all matches, they get 1 point for winning.
- [x] (2 points) After winning the Client goes back to the main menu.
- [x] (2 points) Multiple clients can win together and each get a point for winning.
- [x] (3 points) A tile coordinate is a 2 character string with row first as letter followed by
	column as number, columns are one indexed (0 is invalid) e.g. tile = "d1", row=d,
	column=1. You can assume no more than a 4x4 board size, so rows are a, b, c, d,
	columns 1, 2, 3, 4 – also keep in mind that row * column = even.
- [x] (3 points) You have a couple boards already given in the 'resources folder', you are
	allowed to change the format of these if you do not want the "|" or row column
	information in them to make turning tiles easier. That is up to you. When displayed
	on the Client the letters and column numbers should be displayed though!
- [x] (4 points) Client sends first tile and server evaluates if the request is valid and sends
	back the board with that tile unturned. Handle errors accordingly, e.g. out of
	bounds, tile already turned - use the "error" flag in response for errors.
- [x] (4 points) Client sends second tile (if first tile was success response) and server
	evaluates if the request is valid and sends back the board with that tile unturned.
	Handle errors accordingly, e.g. out of bounds, tile already turned - use the "error"
	flag in response for errors.
- [x] (2 points) Client presents the information well.
- [ ] (2 points) After both turned tiles are shown user presses any key which will lead to
	the Client to just show the current board (with the two tiles if not matches turned
	back again).
- [x] (3 point) Game quits gracefully when option 3 is chosen.
- [x] (3 points) Server does not crash when the client just disconnect (without choosing
	option 3).
- [x] (4 points) You need to run and keep your server running on your AWS instance (or
	somewhere where others can reach it and test it) – if you used the protocol correctly
	everyone should be able to connect with their client. Keep a log of who logs onto your
	server (this logging is already included). You will need to post your exact Gradle
	command we can use (including ip and port) on the channel on Slack #servers.
- [x] (2 points) You test at least 3 other servers with your client. You should comment
	on their servers on Slack, this is how we will grade these two points. – this can be
	done up until 2 days after the official due date.
- [ ] (3 points - extra) In my version the board on the client is only updated after they
	made a "guess", change the implementation so that as soon as the server updates the
	board the client will get this information and will update the board and inform the
	user, e.g. client B makes a correct guess, so the board updates and all other clients
	will get the information about the new board right away.
- [x] (3 points) If user types in "exit" while in the game, the client will exit gracefully.
- [x] (2 points) When sending back an error DO NOT just use error codes but a descriptive
	message that the Client can print. Since others are supposed to be able to use your
	server they might not know your error codes, so print good messages.
- [x] Overall your server/client programs do not crash, handle errors well and are well-structured. No extra points for this, but you might lose up to 5% if this is not the
	case since your programs should be robust at this point.
	NEEDED: On your server always print the board with all tiles unturned, so we can play
	faster when grading. Make sure your program is robust with all possible inputs, we should
	not be able to break it and crash it. We will not be mean when running it but with using
	it to our best ability and basic "gaming" it should not crash.