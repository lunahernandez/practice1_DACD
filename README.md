# Practice 1: Data capture from external sources. OpenWeatherMap
**Course:** Data Science Application Development

**Academic Year:** 2023-2024 

**Degree:** Data Science and Engineering

**School:** Escuela de Ingeniería Informática

**University:** Universidad de Las Palmas de Gran Canaria

## Summary of Functionality
This Java application requests the OpenWeatherMap API, a weather service, every 6 hours to get the weather 
forecast for the next five days at 12pm for each of the eight Canary Islands.

The data obtained is then stored in a SQLite database, in which there is a table for each of the islands, 
and each day has an entry that indicates the temperature, the probability of precipitation, the humidity, 
the clouds and the wind speed.

## Resources Used

- **Development Environments:** IntelliJ IDEA.
- **Version Control Tools:** Git and GitHub.
- **Documentation Tools:** UML for system design, Markdown for project documentation.

## Design
### Design Patterns and Principles Used

The application follows the SOLID design principles for a more maintainable and flexible code structure.

The first principle is the **Single Responsibility Principle (SRP)**, which ensures that each class in the application 
has a clear and defined responsibility. For instance, the `WeatherController` class manages weather control logic, 
`WeatherProvider` supplies weather data, and `WeatherStore` handles weather data storage.

The Open/Closed Principle (OCP) is the second guiding principle.
It is intended to allow classes to be extended without having to change the existing code. An instance of this would 
be the `WeatherProvider` interface, which grants the possibility of introducing new weather providers to the 
`WeatherController` logic without modification.

The third principle is the Liskov Substitution Principle (LSP), where derived classes, for example, 
specified implementations of stores or weather providers, can replace the base class without any alteration 
to the program's behaviour.

The fourth principle is the Interface Segregation Principle (ISP).
Interfaces must be designed specifically, omitting unnecessary methods. For example, the `WeatherProvider` and 
`WeatherStore` interfaces provide only the methods necessary for their respective functions.

The last principle is the Dependency Inversion Principle (DIP), where dependencies are inverted in order to separate 
high-level classes from low-level classes. For example, `WeatherController` depends on interfaces (`WeatherProvider`
and `WeatherStore`) instead of specific implementations.