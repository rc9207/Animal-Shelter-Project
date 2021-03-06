package Database;

import Application.Animal;
import Application.Kennel;
import Application.TaskManager;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;

public class Database {

  public static HashMap<Integer, Animal> animals = new HashMap<Integer, Animal>();
  public static HashMap<Integer, Employee> employees = new HashMap<Integer, Employee>();
  public static HashMap<Integer, Kennel> kennels = new HashMap<Integer, Kennel>();
  public static Employee currentUser; // The user that is currently logged in.

  public static void saveNewPatient(Animal animal) {
    saveNewPatient(
        animal.getName(),
        animal.getSpecies(),
        animal.getTemperment(),
        animal.getSex(),
        animal.getColor(),
        animal.getBreed(),
        animal.getMicrochip(),
        animal.getAge(),
        animal.getWeight(),
        animal.getArrivalDate(),
        animal.getAdoptable());
  }

  public static void saveNewPatient(String name, String species, String temperment, String sex,
      String color, String breed, String microchip, String age,
      String weight, String arrivalDate, String adoptable) {
    try {
      Connection connection = DriverManager.getConnection("jdbc:sqlite:src/database/shelter.db");

      // Ensure the connection exists
      if (connection != null) {
        // Prepare the query statement.
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO animals " +
            "(Name,MainSpecies,Temperment,Sex,Color,Breed,Microchip,Age,Weight,ArrivalDate,Adoptable) "
            +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, name);
        pstmt.setString(2, species);
        pstmt.setString(3, temperment);
        pstmt.setString(4, sex);
        pstmt.setString(5, color);
        pstmt.setString(6, breed);
        pstmt.setString(7, microchip);
        pstmt.setString(8, age);
        pstmt.setString(9, weight);
        pstmt.setString(10,
            (arrivalDate == null ? java.util.Calendar.getInstance().getTime().toString()
                : arrivalDate));
        pstmt.setString(11, adoptable);

        // Execute the query & catch generated key.
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();

        // Create an animal object using the generated key & store it in our hashmap.
        while (rs.next()) {
          animals.put(rs.getInt(1), new Animal(name, species, temperment, sex, color, breed,
              microchip, age, weight, arrivalDate, adoptable, rs.getInt(1)));
        }

        // Close connection
        rs.close();
        pstmt.close();
        connection.close();
      } else {
        throw new Exception("Could not establish connection.");
      }
    } catch (Exception ex) {
      System.out.println(
          "Database Exception: Failed to saveNewPatient.\nReason: " + ex.toString() + "\n\n\n");
      ex.printStackTrace();
    }
  }

  public void removePatient(int animalID, String animalName) {
    try {
      Connection connection = DriverManager.getConnection("jdbc:sqlite:src/database/shelter.db");

      // Ensure the connection exists
      if (connection != null) {
        // Prepare the query statement.
        PreparedStatement pstmt = connection.prepareStatement("DELETE FROM animals " +
            "WHERE CollarID = ? ", PreparedStatement.RETURN_GENERATED_KEYS);

        pstmt.setInt(1, animalID);

        // Execute the query & catch generated key.
        pstmt.executeUpdate();
        System.out.println(animalName + " was removed from database.");
        ResultSet rs = pstmt.getGeneratedKeys();

        // Close connection
        rs.close();
        pstmt.close();
        connection.close();
      } else {
        throw new Exception("Could not establish connection.");
      }
    } catch (Exception ex) {
      System.out.println(
          "Database Exception: Failed to saveNewPatient.\nReason: " + ex.toString() + "\n\n\n");
      ex.printStackTrace();
    }
  }

  public static void updatePatient(int animalID) {

    try {
      Connection connection = DriverManager.getConnection("jdbc:sqlite:src/database/shelter.db");

      // Ensure the connection exists
      if (connection != null) {
        // Prepare the query statement.
        PreparedStatement pstmt = connection.prepareStatement(
            "UPDATE Animals SET Name=?, MainSpecies=?, Sex=?, Color=?, Breed=?, Age=?, Microchip=?, Weight=?, Temperment=?, Adoptable=? WHERE CollarID=?",
            PreparedStatement.RETURN_GENERATED_KEYS);

        pstmt.setString(1, animals.get(animalID).getName());
        pstmt.setString(2, animals.get(animalID).getSpecies());
        pstmt.setString(3, animals.get(animalID).getSex());
        pstmt.setString(4, animals.get(animalID).getColor());
        pstmt.setString(5, animals.get(animalID).getBreed());
        pstmt.setString(6, animals.get(animalID).getAge());
        pstmt.setString(7, animals.get(animalID).getMicrochip());
        pstmt.setString(8, animals.get(animalID).getWeight());
        pstmt.setString(9, animals.get(animalID).getTemperment());
        pstmt.setString(10, animals.get(animalID).getAdoptable());
        pstmt.setInt(11, animals.get(animalID).getAnimalID());

        // Execute the query & catch generated key.
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();

        // Close connection
        rs.close();
        pstmt.close();
        connection.close();

      } else {
        throw new Exception("Could not establish connection.");
      }
    } catch (Exception ex) {
      System.out.println(
          "Database Exception: Failed to saveNewPatient.\nReason: " + ex.toString() + "\n\n\n");
      ex.printStackTrace();
    }
  }

  /**
   * Updates the database with the new kennel information.
   */
  public static void updateKennel(Kennel kennel) {
    try {
      Connection connection = DriverManager.getConnection("jdbc:sqlite:src/database/shelter.db");

      if (connection != null) {
        PreparedStatement pstmt = connection
            .prepareStatement("UPDATE Kennels SET currentAnimal=? WHERE KennelID=?");
        pstmt.setInt(1,
            (kennel.getCurrentAnimal() == null) ? -1 : kennel.getCurrentAnimal().getAnimalID());
        pstmt.setInt(2, kennel.getKennelID());
        pstmt.executeUpdate();

        // Close connection
        pstmt.close();
        connection.close();
      } else {
        throw new Exception("Could not establish connection.");
      }
    } catch (Exception ex) {
      System.out.println(
          "Database Exception: Failed to updateKennel.\nReason: " + ex.toString() + "\n\n\n");
      ex.printStackTrace();
    }
  }

  /**
   * Loads Animals and Kennels from the database.
   */
  public static void loadAll() {
    try {
      Connection connection = DriverManager.getConnection("jdbc:sqlite:src/database/shelter.db");

      if (connection != null) {
        Statement statement = connection.createStatement();

        // Load animals and place them into the HashMap
        ResultSet resultSet = statement.executeQuery("SELECT * FROM animals");
        while (resultSet.next()) {
          animals.put(resultSet.getInt("CollarID"), new Animal(
              resultSet.getString("Name"),
              resultSet.getString("MainSpecies"),
              resultSet.getString("Temperment"),
              resultSet.getString("Sex"),
              resultSet.getString("Color"),
              resultSet.getString("Breed"),
              resultSet.getString("Microchip"),
              resultSet.getString("Age"),
              resultSet.getString("Weight"),
              resultSet.getString("ArrivalDate"),
              resultSet.getString("Adoptable"),
              resultSet.getInt("CollarID")));
        }

        // Load kennels and place them into the HashMap
        resultSet = statement.executeQuery("SELECT * FROM kennels");
        while (resultSet.next()) {
          int kennelID = resultSet.getInt("KennelID");
          int currentAnimal = resultSet.getInt("CurrentAnimal");

          Kennel kennel;
          if (currentAnimal == -1) {
            // No animal, so empty constructor.
            kennel = new Kennel(kennelID);
          } else {
            // get animal info from animals hashmap where key = currentAnimal id
            kennel = new Kennel(kennelID, animals.get(currentAnimal));
          }
          kennels.put(kennelID, kennel);
        }

        // Load employees and place them into the HashMap
        resultSet = statement.executeQuery("SELECT * FROM users");
        while (resultSet.next()) {
          Employee employee = new Employee(
              resultSet.getInt("ID"), resultSet.getString("Username"),
              resultSet.getString("Name"), resultSet.getBoolean("Manager"));
          employees.put(resultSet.getInt("ID"), employee);
        }

        // Load tasks and place them into the TaskManager
        resultSet = statement.executeQuery("SELECT * FROM Tasks");
        while (resultSet.next()) {
          TaskManager.existingTasks.put(resultSet.getInt(1),
              new TaskManager.Task(resultSet.getInt(1),
                  resultSet.getString(2),
                  resultSet.getString(3)));
        }

        // Loads Assigned tasks and places them in the TaskManager
        resultSet = statement.executeQuery("SELECT * FROM AssignedTasks");
        while (resultSet.next()) {
          TaskManager.assignedTasks.put(resultSet.getInt(1),
              new TaskManager.AssignedTask(resultSet.getInt(1),
                  TaskManager.existingTasks.get(resultSet.getInt(2)),
                  employees.get(resultSet.getInt(4)),
                  kennels.get(resultSet.getInt(5)),
                  animals.get(resultSet.getInt(3))));
        }

        // Close all connections.
        resultSet.close();
        statement.close();
        connection.close();
      } else {
        throw new Exception("Could not establish connection.");
      }
    } catch (Exception ex) {
      System.out
          .println("Database Exception: Failed to loadAll.\nReason: " + ex.toString() + "\n\n\n");
      ex.printStackTrace();
    }
  }

  public static int saveNewTask(TaskManager.Task task) {
    try {
      Connection connection = DriverManager.getConnection("jdbc:sqlite:src/database/shelter.db");

      // Ensure the connection exists
      if (connection != null) {

        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO Tasks " +
                "(TaskName, TaskDescription) VALUES (?,?)",
            PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, task.getTaskName());
        pstmt.setString(2, task.getTaskDescription());

        // Execute the query & catch generated key.
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();

        while (rs.next()) {
          return rs.getInt(1);
        }
      } else {
        throw new Exception("Could not establish connection.");
      }
    } catch (Exception ex) {
      System.out.println(
          "Database Exception: Failed to saveNewTask.\nReason: " + ex.toString() + "\n\n\n");
      ex.printStackTrace();
    }
    return -1;
  }

  public static int saveNewAssignedTask(TaskManager.AssignedTask task) {
    try {
      Connection connection = DriverManager.getConnection("jdbc:sqlite:src/database/shelter.db");

      // Ensure the connection exists
      if (connection != null) {

        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO AssignedTasks " +
                "(TaskID, AnimalID, EmployeeID, KennelID ) VALUES (?,?,?,?)",
            PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, task.task.getTaskID());
        pstmt.setInt(2, task.animal.getAnimalID());
        pstmt.setInt(3, task.employee.getId());
        pstmt.setInt(4, task.kennel.getKennelID());


          // Execute the query & catch generated key.
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();

        while (rs.next()) {
            task.setID(rs.getInt(1));
          return rs.getInt(1);
        }
      } else {
        throw new Exception("Could not establish connection.");
      }
    } catch (Exception ex) {
      System.out.println(
          "Database Exception: Failed to saveNewAssigned.\nReason: " + ex.toString() + "\n\n\n");
      ex.printStackTrace();
    }
    return -1;
  }

  /**
   * Method to save a newly created employee to the database.
   *
   * @param employee Employee object created by the "Create New User" FXML page.
   * @param password String version of the password from the "Create New User" FXML page. It is
   *                 encrypted when saved.
   */
  public static void saveNewEmployee(Employee employee, String password) {
    try {
      Connection connection = DriverManager.getConnection("jdbc:sqlite:src/database/shelter.db");

      // Ensure the connection exists
      if (connection != null) {
        // Prepare the query statement.
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO Users " +
                "(Username, Password, Name, Manager) VALUES (?, ?, ?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, employee.getUsername().toLowerCase());
        pstmt.setString(2, encrypt(password));
        pstmt.setString(3, employee.getName());
        pstmt.setInt(4, employee.isManager() ? 1 : 0);

        // Execute the query & catch generated key.
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();

        while (rs.next()) {
          employee.setId(rs.getInt(1));
          employees.put(employee.getId(), employee);
        }

        // Close connection
        rs.close();
        pstmt.close();
        connection.close();
      } else {
        throw new Exception("Could not establish connection.");
      }
    } catch (Exception ex) {
      System.out.println(
          "Database Exception: Failed to saveNewEmployee.\nReason: " + ex.toString() + "\n\n\n");
      ex.printStackTrace();
    }
  }

  public static boolean tryLogin(String user, String pass) {
    try {
      Connection connection = DriverManager.getConnection("jdbc:sqlite:src/database/shelter.db");

      if (connection != null) {
        Statement statement = connection.createStatement();
        ResultSet rs = statement
            .executeQuery(String.format("SELECT * FROM USERS WHERE Username='%s' " +
                "AND Password='%s'", user.toLowerCase(), encrypt(pass)));

        while (rs.next()) {
          // Found the user, so create the employee object
          currentUser = new Employee(rs.getInt("ID"), rs.getString("Username"),
              rs.getString("Name"), rs.getBoolean("Manager"));
          System.out.println(currentUser);
          return true;
        }
      } else {
        throw new Exception("Could not establish connection.");
      }
    } catch (Exception ex) {
      System.out
          .println(
              "Database Exception: Failed to tryLogin.\nReason: " + ex.toString() + "\n\n\n");
      ex.printStackTrace();
    }
    // If we reach this point, either the connection failed, or we didn't find the user.
    return false;
  }

  private static String encrypt(String string) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    BigInteger bi = new BigInteger(1, md.digest(string.getBytes(StandardCharsets.UTF_8)));
    StringBuilder sb = new StringBuilder(bi.toString(16));

    while (sb.length() < 32) {
      sb.insert(0, '0');
    }
    return sb.toString();
  }
}
