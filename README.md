# Everything Books
This project aims to connect all the book readers and lovers to the publishers, bookstores and libraries via a single platform. It also aims to provide mobility between these enterprises via Delivery Companies.

## Team Members
1. [Amisha Kalhan](https://github.com/amisha109) - 002790306
2. [Komal Suryan](https://github.com/komalsuryan) - 002747707

## Enterprises
There are 4 enterprises involved in this application
1. Seller Enterprise
2. Rental Enterprise
3. Delivery Enterprise
4. General Population

### Seller Enterprise
Seller enterprise consists of all the organizations that have the capability to sell books to other organizations and enterprises.
### Rental Enterprise
This enterprise consists of organizations that can lend books to other enterprises for a fixed period of time. They don’t have selling capabilities.
### Delivery Enterprise
This enterprise consists entirely of organizations that deliver things from one place to another. They provide connectivity among the seller organizations, rental organizations and the general population.
### General Population
General Population is an abstract enterprise consisting of all the users who belong to the general population and don’t have any roles in the above enterprises. The members of this enterprise will act as users for our application.
## Organizations
There can be several organizations under each enterprise. For the sake of demonstration of the project, necessary ones have been taken into account.
### Organizations under the Seller Enterprise
1. Publishing Companies
2. BookStores
### Organizations under Rental Enterprise
1. Libraries
### Organizations under Delivery Enterprise
1. Delivery Companies
### Organizations under General Population
1. Users Directory
2. Employees Directory
## System Roles
1. __Network Admin__
   - Add, Edit and Remove Publishers
   - Add, Edit and Remove Delivery Companies
   - Add, Edit and Remove BookStores
   - Add, Edit and Remove Users
2. Publishing Company Admin
   - Add, Edit and Remove books published under the publisher
   - Add, Edit and Remove Employees
   - View Orders that have been placed
3. BookStore Admin
   - Add, Edit and Remove books and their stocks
   - Purchase new books
   - Add, edit and remove employees working for the book store
   - View sales
4. Delivery Company Admin
   - Add, edit and remove employees working for the company
   - View the deliveries made under the company
5. Library Admin
   - Manage employees working under the company
   - Manage and purchase books for the library
   - View the book rentals
6. Publishing Company Employees
   - Process and update orders
   - Add and update books under the publisher
   - Schedule delivery if needed
7. BookStore Employees
   - Process the orders placed by the users
   - Schedule delivery if needed
   - Delivery Company Employees
   - Pickup and deliver orders
8. Library Employee
   - Approve rentals
   - Manage late returns
   - Send reminder emails
   - Send invoice
9. Users
   - Search for books available
   - Order from publisher
   - Order from bookstore
   - Rent from library
