# Assignment 4 Activity 1

## [Video Link](https://youtu.be/mhCI3Bsdbic)

## Description
Task 1 - Server
Task 2 - Threaded Server
Task 3 - Bounded Thread Server

All servers have the below functionality:
- add a string to the current list and return it
- clear the list and return it
- find a string in the current list and return the index of it, -1 if not found
- display the current list
- sort the current list alphabetically
- prepend a string onto an existing string at a particular index

Client sends request with appropriate data to server for processing.

## Protocol

### Requests
request: { "selected": <int: 1=add, 2=clear, 3=find, 4=display, 5=sort, 6=prepend
0=quit>, "data": <thing to send>}

  add: data <string> -- a string to add to the list
  clear: data <> -- no data given, clears the whole list
  find: data <string> -- display index of string if found, else -1
  display: data <> -- no data given, displays the whole list
  sort: data <> -- no data given, sorts the list
  prepend: data <int> <string> -- index and string, prepends given string to string at index

### Responses

success response: {"ok" : true, type": <String> "data": <thing to return> }

type <String>: echoes original selected from request
data <string>: 
    add: return current list
    clear: return empty list
    find: return integer value of index where that string was found or -1
    display: return current list
    sort: return current list
    prepend: return current list


error response: {"ok" : false, "message"": <error string> }
error string: Should give good error message of what went wrong


## How to run the program
### Terminal
Base Code, please use the following commands:
```
    For Task 1, run "gradle runTask1 -Pport=8000 -q --console=plain"
```
```
    For Task 2, run "gradle runTask2 -Pport=8000 -q --console=plain"
```
```
    For Task 3, run "runTask3 -Ppool=12 -Pport=8000 -q --console=plain"
```
```   
    For Client, run "gradle runClient -Phost=localhost -Pport=8000 -q --console=plain"
```   



