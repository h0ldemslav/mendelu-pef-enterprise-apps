# Airline Reservation System

This is a project for Enterprise applications course LS 2023/2024. The goal is to implement a system for managing airline’s customers, flights, tickets, etc. 

### Dataset import
1. Run postgres (via postgres cmd or docker)
2. Create db - `airline` (port 5432)
3. Connect to that db
4. Run the app to apply migrations
5. Import each table from folder `db_data` using postgres `COPY` command in console

```sql
COPY airport 'PROJECT_PATH/db_data/airport.csv' WITH CSV HEADER;
```

### Main entities

Customer:

  - id
  - first_name
  - last_name
  - credit
  - phone
  - email
  - password (hash)


Flight:

  - id 
  - number 
  - departure
  - arrival
  - status
  - delay
  - airport_departure_id
  - airport_arrival_id
  - aircraft_id
  - fare_tariff_id


Fare_tariff:

  - id
  - code
  - business_price
  - premium_price
  - economy_price


Airport:

  - id
  - code
  - name
  - country_code
  - region_code
  - municipality
  - gps_code
  - latitude
  - longitude


Aircraft:

  - id
  - code
  - model
  - business_capacity
  - premium_capacity
  - economy_capacity


Ticket:

  - id
  - number
  - class
  - price
  - discount
  - price_after_discount
  - seat_number
  - passenger_full_name
  - departure
  - arrival
  - flight_id
  - customer_id

### Operations

- Basic CRUD operations for each entity
- A bit advanced operations
    - Seat generation 
    - Change seat assignment, if available
    - Upgrade ticket class, if possible
    - Transfer to another flight
    - Flight cancellation (make flight cancelled and give passengers a discount)
    - Flight reports (ticket revenue for a certain flight, ticket class distribution, passenger load factor)
    - Simple flight recommendations