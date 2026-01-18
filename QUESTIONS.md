# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```
"I would refactor the Store and Product modules to use the Repository Pattern instead of Active Record (PanacheEntity).
Currently, Store and Product entities mix database logic (persist()) with domain data. 
This violates the Single Responsibility Principle and makes unit testing difficult because we cannot easily mock the static database methods. 
The Warehouse module uses the Repository pattern (separating DbWarehouse from domain Warehouse), which allows for pure unit tests using Mockito, 
as demonstrated in my implementation. Standardizing on the Repository pattern would make the codebase more maintainable and testable."
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```
"I prefer the OpenAPI (Contract-First) approach used in the Warehouse API over the Code-First approach used in Store/Product.
Pros of OpenAPI:
Clear Contract: It forces a strict agreement on the API structure before coding begins.
Parallel Development: Frontend and Backend teams can work simultaneously using the mocked YAML spec.
Validation: Request objects (DTOs) are auto-generated, ensuring strict type safety.
Cons: Initial setup is verbose.
Choice: For enterprise systems, Contract-First is superior to prevent 'API Drift' where documentation mismatches the actual code."
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```
"To balance quality and speed, I applied the Testing Pyramid:
Unit Tests (High Volume): Focused heavily here (e.g., CreateWarehouseUseCaseTest). These are fast and cover 100% of business rules (capacity checks, validations). This ensures the core logic is bug-free.
Integration Tests (Medium Volume): Used @QuarkusTest (e.g., StoreResource) to verify the 'Critical Paths'—specifically database transactions and wiring.
JaCoCo: Integrated to monitor coverage, ensuring we meet the 80% threshold on business logic classes."

```