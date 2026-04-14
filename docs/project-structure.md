# Car Dealership Project Structure

## Suggested layers

- `controller`: Java servlets that receive requests and return views
- `dao`: plain Java classes that contain JDBC database code
- `service`: simple business logic and validation before calling the DAO layer
- `model`: Java entity classes
- `util`: shared helpers such as database connection management
- `src/main/webapp`: JSP pages and static frontend files
- `database`: SQL schema and sample data
- `docs`: project documentation and ERD notes

For the beginning, the service layer should stay simple and only contain basic validation and method calls to the DAO layer.
