An miniature app that retrieves online .json data.

Interesting facts:

- Kotlin-based
- Used SQLite for data storage
- Used RecyclerView to display the data tidily
- Implemented retry logic for Internet connection errors
- Tried to implement browse-in-pages mode (e.g., jump to the 5th page which corresponds to row 101-125) at the pages branch (here) but bugs are to be fixed. A bug-free but less user-friendly implementation (infinite scrolling) can be found at the master branch.


TODOs if time permitted:

- dark mode
- a switch button between infinite scrolling (i.e., scroll down to the last row within one page) and browse-in-page mode (i.e., divide the rows into pages of 25 lines)
- improve UI design
- enable the user to enter other .json addresses
- more detailed comments
- sleep more...🥵🥵🥵
