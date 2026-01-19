# Case Study Scenarios

---

## Scenario 1: Cost Allocation and Tracking

### Situation
The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

### Task
Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Consider important aspects, relate them to prior experience, and elaborate on relevant questions and considerations.

### Questions and Considerations

The primary challenge in cost allocation within a fulfillment environment is maintaining **data integrity across distributed boundaries**. Costs such as labor and overhead often reside in external ERP systems, while inventory movements are tracked within the fulfillment system.

**Key Considerations:**

- **Atomic Consistency**  
  To ensure accurate cost data, I implemented the *Transaction Synchronization Registry* pattern in the Stores module. This guarantees that notifications to external cost-tracking systems are triggered only after a successful database commit, preventing ghost or duplicate expenses.

- **Granularity of Cost Allocation**  
  A decision must be made on whether to allocate costs at:
    - **Unit level** – higher accuracy, higher latency
    - **Batch level** – better performance, slightly reduced precision

**Questions for Stakeholders:**

- What is the acceptable delay for cost reconciliation?
- Can we leverage an asynchronous **fan-out pattern using Kafka** to update multiple cost centers simultaneously?

---

## Scenario 2: Cost Optimization Strategies

### Situation
The company aims to reduce fulfillment operation costs without compromising service quality.

### Task
Discuss potential cost optimization strategies, expected outcomes, and how to identify, prioritize, and implement them.

### Questions and Considerations

Cost optimization requires balancing **automation capabilities** with **physical and operational constraints**.

**Proposed Strategies:**

- **Predictive Unit Replacement**  
  Using the *Replace* functionality implemented in the Warehouses module, high-maintenance Business Units can be replaced with modern units while retaining the same Business Unit Code. This allows direct ROI comparison between old and new units.

- **Inventory Velocity Mapping**  
  Leverage location data to identify under-utilized capacity in high-cost cities (e.g., Amsterdam) and shift fulfillment loads to regional hubs with better capacity utilization.

**Prioritization Approach:**

- Rank initiatives using the **Cost-to-Serve** metric.
- Implement strategies through new *Use Cases* in a **Hexagonal Architecture**, ensuring optimization logic is added without modifying core CRUD flows.

---

## Scenario 3: Integration with Financial Systems

### Situation
The Cost Control Tool must integrate with existing financial systems to support real-time synchronization and reporting.

### Task
Discuss the importance, benefits, and implementation approach for seamless financial system integration.

### Questions and Considerations

Integration with financial systems such as SAP or Oracle is critical for **real-time P&L visibility**.

**Implementation Strategy:**

- **Hexagonal Architecture (Ports & Adapters)**  
  By defining a `WarehouseStore` port, financial adapters can translate domain data into proprietary financial formats without contaminating business logic.

- **Event-Driven Synchronization**  
  Use Kafka for synchronization. For example, when a `WarehouseCreated` event is emitted, the financial system consumes it to initialize a corresponding cost center. This approach naturally handles back-pressure during maintenance or downtime.

---

## Scenario 4: Budgeting and Forecasting

### Situation
The company needs accurate budgeting and forecasting to predict future costs and allocate resources effectively.

### Task
Explain the importance of budgeting and forecasting and key factors in designing a supporting system.

### Questions and Considerations

Accurate forecasting depends heavily on **high-quality historical data**.

**Key Design Considerations:**

- **Historical Data Preservation**  
  In the `ArchiveWarehouseUseCase`, a soft-delete approach is used to retain historical records and metadata (Business Unit Code, Location). This preserved data serves as training input for AI-driven forecasting models.

- **Constraint-Based Budgeting**  
  The system enforces constraints such as `maxCapacity` and `maxNumberOfWarehouses` defined in the Location entity, preventing unrealistic or physically impossible budget plans.

---

## Scenario 5: Cost Control in Warehouse Replacement

### Situation
An existing Warehouse is being replaced with a new one that reuses the same Business Unit Code. The old Warehouse is archived, but its cost history must be retained.

### Task
Discuss cost control aspects, the importance of preserving history, and budget enforcement mechanisms.

### Questions and Considerations

Warehouse replacement is a **high-capex operation**, requiring strong cost controls to ensure continuity.

**Why Preserve Cost History?**

- Reusing the Business Unit Code enables *apples-to-apples* cost comparison.
- Management can directly evaluate whether the new Warehouse operates within historical budget benchmarks.

**Budget Safeguards:**

- In the `ReplaceWarehouseUseCase`, a validation rule ensures:  
  `newWarehouse.capacity >= oldWarehouse.stock`
- This prevents capacity gaps that would otherwise force emergency overflow storage, instantly breaking the project budget.

---

## Instructions for Candidates

Before starting the case study, read **[BRIEFING.md](BRIEFING.md)** to understand the domain, entities, business rules, and constraints.

### Analysis Guidance

- Carefully analyze each scenario.
- Identify the key information required before defining the project scope.
- Focus on bridging **technical decisions with business value**.
- Keep discussions high-level; deep dives are not required.
