# Mazes
Random Maze Generation and Breadth first/Depth first Searching

This project completed in Fundamentals of Computer Science 2. It is a Java program that generates a random,
solvable maze with no cycles by creating a minimum spanning tree of the edges in the maze using Kruskal's
algorithm. You may solve the maze by having the program run depth first search, breadth first search, or
traversing the maze manually. It renders the maze, lights up visited cells, and upon reaching the end it
draws back the shortest path back to the start of the maze.

CONTROLS: r - Resets the Maze completely, d - Begins depth first search, b - Begins breadth first search, m - Begins manual traversal
(when in manual traversal),
 arrow keys - traverse the cell

CHANABLE CONSTANTS:
scale - makes the board larger or smaller, 
blocksWide - the number of columns of cells in the maze, 
blocksHigh - the number of rows of cells in the maze, 
