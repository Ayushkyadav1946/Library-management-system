# Project Report: Library Management System

## 1. Objective

The Library Management System is a tool that combines core data structures and algorithms—a hash table, a search tree, a binary heap, a queue and classic sorting and searching methods—to solve one realistic problem instead of handling each idea separately. The Library Management System keeps track of books, members, loans, fines and reservation waitlists.

## 2. Problem statement

1. Look up a book or member instantly by ID when staff are processing a transaction.

2. Search Library Management Systems catalog by title.

3. Always know, without scanning every loan, which loans due back soonest and which loans are currently overdue.

4. Let members reserve a book that is fully checked out and automatically hand the returned copy to whoever has waited

5. Sort the catalog on demand by fields (title, author, year).

6. Persist all of this between runs without requiring a database server.

## 3. Design and data structures used

### 3.1 Hash table. `MyHashMap<K, V>`

The Library Management System builds this hash table using an array of buckets and separate chaining to resolve collisions and the hash table grows automatically when the load factor goes over 0.75. The hash table delivers average O(1) put, get and remove operations. The Library Management System uses the hash table for three lookups: to find a Book by id a Member by id and a Loan by id. The Library Management System also uses the hash table inside itself to link a book id to its reservation queue.

**Why not `java.util.HashMap`?** The Library Management System built its hash table so that the developer could see how hashing, bucket indexing chaining and resizing work. The Library Management System did not rely on the library for this part.

### 3.2 Binary search tree. `BookCatalogBST`

The Library Management System uses a search tree keyed on book title ignoring case. The Library Management System supports insert, search, delete (using the in‑order successor when a node has two children). An inOrder() traversal that returns all books sorted by title in O(n). The Library Management System finds a book in about O(log n) time on average. If titles are added in already‑sorted order the search can become O(n). This limitation is present. The Library Management System could later adopt a tree such as AVL or Red‑Black.

### 3.3 Binary min-heap. `MinHeap<T>`

The Library Management System uses a heap that keeps loans in order of due date. The Library Management System supports insert, peekMin and extractMin all taking O(log n) time. The Library Management System also adds a remove(item) operation that can delete any loan in O(log n) time using a hash map that records each loan's position in the array. This feature is needed because when a loan is returned the Library Management System must remove it from wherever it's in the heap not only from the top. A simple heap would only allow removal of the minimum.

### 4. Queue. `MyQueue<T>`

The Library Management System implements a queue as a linked list with head and tail pointers providing O(1) enqueue and dequeue. The Library Management System uses this queue as a FIFO reservation waitlist, for each book: the first member to reserve is the one served when a copy returns.

### 5. Sorting. `SortAlgorithms`

The Library Management System implements two sorting algorithms: merge sort, which's stable and runs in O(n log n) time with O(n) extra space; and quick sort, which works in place and also runs in O(n log n) on average using a random pivot to avoid the worst‑case O(n²) on sorted data. Both algorithms work generically over a Comparator<T>. The Library Management Systems command‑line interface offers a "list books" command that lets a user choose which algorithm to run and which book field to sort by so the two can be compared on the set of books.

### 6. Searching. `SearchAlgorithms`

The Library Management System implements search that runs in O(log n). Requires a sorted list. It also offers search that runs in O(n) and works on unsorted lists. For catalog searching the Library Management System first looks in the binary search tree, which takes O(h) time. If the book is not found the Library Management System then uses a merge sort followed by search as a backup.

This is a demonstration of the algorithm on the same data.

## 4. System architecture

```
Main (CLI) → LibraryService → MyHashMap / BookCatalogBST / MinHeap / MyQueue
                    ↓
              FileStorage (plain-text persistence)
```

LibraryService is the place that keeps all five structures in sync. For example when a book is issued Library Service reduces the books copy count creates a Loan puts it into the MinHeap and records it in the loan hash map all inside one method. This way no caller can change one structure without touching the others.

## 5. Complexity summary

| Operation | Structure | Time complexity |
|---|---|---|
| Find book/member/loan by id | MyHashMap | O(1) average, O(n) worst case |
| Insert/search/delete book by title | BookCatalogBST | O(h): O(log n) average, O(n) worst case |
| Browse catalog sorted by title | BookCatalogBST in-order traversal | O(n) |
| Find most-overdue loan | MinHeap | O(1) peek, O(log n) after a change |
| Remove a specific loan from the heap (on return) | MinHeap | O(log n) |
| Join/leave a reservation waitlist | MyQueue | O(1) |
| Sort catalog by any field | Merge sort / Quick sort | O(n log n) average |
| Search catalog by title (sorted) | Binary search | O(log n) |

## 6. Testing performed

The system was tested from start to finish using scripted input on the command line that covers:

- Adding books and members then showing the lists.

- Issuing a book until no copies remain confirming that a waitlist entry is created.

- Returning that book. Checking that the next copy is automatically issued to the member using MinHeap.remove MyQueue.dequeue and re-issue.

- Sorting the catalog by title with merge sort and by year with sort and checking that the order is correct for both.

- Searching for an existing title using the BST.

- Forcing a loans date into the past and confirming that the overdue view shows the correct number of late days and the fine amount ($0.25 per day) and that returning the book calculates the same fine.

- Trying to remove a book that still has copies on loan and confirming that the removal is correctly refused.

- Restarting the program between steps and confirming that all state, including the reservation waitlist is correctly reloaded from disk.

All of the above produced the expected output.

## 7. Possible extensions

- Balance the BST (AVL or Red-Black) to guarantee O(log n) worst‑case search.

- Support searching or filtering by author or year using an index structure.

- Replace the text‑file persistence with an embedded database.

- Add a graph‑based recommendation feature that shows readers who borrowed this borrowed…

## 8.

The project shows five data structures and three algorithm families, each chosen because it is the tool, for a need of the Library Management System instead of being used just as a textbook example. All components were built from principles. Checked with scripted end‑to‑end testing.
