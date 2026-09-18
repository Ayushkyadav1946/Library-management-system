# Library Management System

This is a Library Management System that I built in Java as a DSA project.

The project is a command line program so everything is performed through the terminal. I built this project to observe how data structures and algorithms can be applied in a program, not just for practice.

I built all the data structures myself. I did not rely on built‑in structures such as HashMap, TreeMap, PriorityQueue or Collections.sort() in this Library Management System.

## What does Library Management System do?

Library Management System handles some library operations, such as:

- Adding books
- Listing all books
- Searching for books
- Removing books
- Adding library members
- Issuing books
- Returning books
- Calculating return fines
- Managing a waiting list for unavailable books
- Showing loans
- Saving the library data between program runs

When a book is issued the normal loan period is 14 days.

If the book is returned after the date the fine is $0.25 per day.

There is also a waiting list system. If all copies of a book are currently issued a member can join the waiting list. When a copy is returned it is given to the member who has been waiting the longest.

## Data. Algorithms

I used data structures based on what each part of this Library Management System required.

| Concept | File | Used for |
|---|---|---|
| Hash Table | datastructures/MyHashMap.java | Finding books, members and loans by ID |
| Binary Search Tree | datastructures/BookCatalogBST.java | Storing the book catalog by title |
| Min Heap | datastructures/MinHeap.java | Keeping track of loans by date |
| Queue | datastructures/MyQueue.java | Managing book waitlists |
| Merge Sort | algorithms/SortAlgorithms.java | Sorting books |
| Quick Sort | algorithms/SortAlgorithms.java | Another way of sorting books |
| Binary Search | algorithms/SearchAlgorithms.java | Finding a title in books |
| Linear Search | algorithms/SearchAlgorithms.java | searching and comparison |

### Hash Table

The hash table is my implementation. It uses chaining to resolve collisions. It can also resize when it becomes too full.

I mainly use the hash table when I need to find something by ID for example a book, member or loan.

### Binary Search Tree

The book catalog is stored in a Binary Search Tree.

Books are stored according to their titles. This also allows me to retrieve books in order using an in‑order traversal.

One limitation is that the tree is not balanced. Depending on the order in which books are added the tree can become less efficient.

### Min Heap

The Min Heap is used to store loans.

The goal is to make the loan, with the date easy to find. This is useful when I display loans or determine which loan comes next.

### Queue

I used a linked queue to manage the waiting list.

The queue follows FIFO – First In, First Out. This means the member who joins the waiting list first receives the returned book first.

### Searching

I implemented both Merge Sort and Quick Sort using Javas built‑in sorting method.

Merge Sort offers O(n log n) performance. Is stable.

Quick Sort uses a pivot. Has O(n log n) average‑case performance.

I also added Binary Search and Linear Search to compare these two approaches.

## Library Service

The different parts of this Library Management System are connected through:

`service/LibraryService.java`

This file handles most of the library operations.

For example when I issue a book the service must find the book find the member create the loan update the number of copies and update the required data structures.

The service layer is basically the part that makes all the different components work together.

## Requirements

I need JDK 11 or newer.

To check if Java is installed, run:

```
java -version
javac -version
```

If I need to install Java I can get Eclipse Temurin from:

Eclipse Temurin / Adoptium

For Ubuntu JDK 21 can also be installed using:

```
sudo apt-get install openjdk-21-jdk
```

There are no dependencies. A database, build tool or internet connection is not required to run this Library Management System.

## Running the Project

1. Clone the repository

```
git clone https://github.com/<your-username>/<your-repo-name>.git
cd <your-repo-name>
```

2. Compile it

From the project folder run:

```bash
javac -d bin $(find src -name "*.java")
```

If I am using Windows PowerShell, use:

```powershell
javac -d bin (Get-ChildItem -Recurse *.java src | ForEach-Object { $_.FullName })
```

After you compile the files they will be inside the bin folder.

### Run the program

```bash
java -cp bin com.library.Main
```

You should then see the menu.

Just type the number of the operation you want to perform and follow the instructions that appear in the terminal.

For example you can add a book add a member and then issue that book to the member.

## Saving Data

I did not want the data to disappear each time the program closed. I added a storage system.

The program stores data inside the data/ folder.

The files are text files so there is no database involved.

The program saves the data automatically. The program loads the data again when it starts.

If you want to start over with the library just delete everything inside the data/ folder.

You can also delete the folder. The program will create the folder again when it needs the folder.

## Project Structure

```
library-management-system/
│
├── README.md
├── REPORT.md
├── data/
│
└── src/com/library/
    │
    ├── Main.java
    │
    ├── model/
    │   ├── Book.java
    │   ├── Member.java
    │   └── Loan.java
    │
    ├── datastructures/
    │   ├── MyHashMap.java
    │   ├── BookCatalogBST.java
    │   ├── MinHeap.java
    │   └── MyQueue.java
    │
    ├── algorithms/
    │   ├── SortAlgorithms.java
    │   └── SearchAlgorithms.java
    │
    ├── util/
    │   └── FileStorage.java
    │
    └── service/
        └── LibraryService.java
```

## Example

This is roughly what the program looks like when adding a book:

```
---- MENU ----
1. Add a book
2. List books
3. Search for a book by title
4. Add a member
5. List members
6. Issue a book to a member
7. Return a book
8. View loans
9. Remove a book
0. Save and exit

Choose an option: 1

Dune
Author: Frank Herbert
Year published: 1965
Number of copies: 2

Added: [#1] "Dune" by Frank Herbert (1965). 2/2 Available
```

## Limitations

The program has limitations that I'm aware of.

The biggest limitation is the Binary Search Tree. The BST can become like a linked list if books are inserted in a particular order. In that situation searching inserting or deleting can take O(n) time of O(log n).

A future version could use an AVL tree to solve this.

Another limitation is the way data is stored. I am using pipe‑delimited text files of an actual database.

I chose this because setting up a database would add complexity and dependencies while the main focus of the program was data structures and algorithms.

## Why I Made This Project

The reason I made the program was to get more comfortable with DSA.

It is easy to understand a hash table, queue, heap, BST or sorting algorithm when looking at them separately. The interesting part for me was figuring out where they actually make sense in the program.

For example the queue is useful for a waiting list the heap works well for keeping track of dates. The hash table makes looking up records, by ID faster.

The program is still a command-line application. It is not meant to be a real‑world library system. The program was mainly built as a way to practice DSA concepts by putting them into something that actually works.
