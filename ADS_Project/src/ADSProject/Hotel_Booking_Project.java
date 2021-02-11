
package ADSProject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner; 

public class Hotel_Booking_Project {
	public static HashMap<String, Integer> rooms = new HashMap<String, Integer>();
	public static HashMap<Integer, Integer> roomCost = new HashMap<Integer, Integer>();
	public static HashMap<Integer,Boolean> r = new HashMap<Integer,Boolean>();

	public static HashMap<Integer, Integer> roomId = new HashMap<Integer, Integer>();
	public static int roomNumber, custPhoneNo, days, cost, hotelId, room_Id, cust_id, booking_id; 
	
	public static String username, role, option, location, hotel_name, custName, custAddress, custEmail, startEndDate, booking;
	public static boolean custPresent = false;
	
	public static int roomCount = 0;
	public static Scanner in = new Scanner(System.in);
	static String url = "jdbc:ucanaccess://D://Projects//EclipseJavaProjects//ADS_Project_DB.accdb";
	public static Connection con;
	public static Statement s;
	public static String fetchHotels(String location) {
		String s = "select * from Hotel where Lower(location) like '%" + location.toLowerCase() + "%' and rooms_available is true";
		return s;
	}
	
//	Retrieving available rooms for the hotel Id entered by the user
	public static String selectHotel(int hotelId) {
		String fetchRooms = "select * from Room where Room.hotel_id = " + String.valueOf(hotelId) + " and status is true";
		return fetchRooms;
	}
	
	public static void insertIntoTables(Connection con, String startQuery, String endQuery, int days, String custName, String custAddress, int custPhoneNo, String custEmail, String hotel_name, int roomNumber, int cost, String location, int roomCount, int roomId) throws ParseException, SQLException {
		if(cust_id == 0) {			
//			"Inserting in Customer table"
			String custQuery = " insert into customer (cust_name, address, phone, email)"
	    	        + " values (?, ?, ?, ?)";
	    	      PreparedStatement preparedStmt1 = con.prepareStatement(custQuery);
	    	      preparedStmt1.setString (1, custName);
	    	      preparedStmt1.setString (2, custAddress);
	    	      preparedStmt1.setInt    (3, custPhoneNo);
	    	      preparedStmt1.setString (4, custEmail);
	    	      preparedStmt1.execute();
	    	      try (ResultSet rs2 = preparedStmt1.getGeneratedKeys()) {
	    	    	    if (rs2.next()) {
	    	    	        cust_id = rs2.getInt(1);
	    	    	    }
	    	      }
	    	    updateLoginTable();
		}
//		Inserting the booking details into Booking table
		String query = " insert into booking (cust_id, room_id, cost, location, days, startdate, enddate)"
    	        + " values (?, ?, ?, ?, ?, ?, ?)";
    	      PreparedStatement preparedStmt = con.prepareStatement(query);
    	      preparedStmt.setInt    (1, cust_id);
    	      preparedStmt.setInt    (2, roomId);
    	      preparedStmt.setInt    (3, cost);
    	      preparedStmt.setString (4, location);
    	      preparedStmt.setInt    (5, days);
			  preparedStmt.setDate   (6, java.sql.Date.valueOf(startQuery));
			  preparedStmt.setDate   (7, java.sql.Date.valueOf(endQuery));
    	      preparedStmt.execute();	
    	      try (ResultSet rs3 = preparedStmt.getGeneratedKeys()) {
  	    	    if (rs3.next()) {
  	    	    	System.out.println();
  	    			System.out.println("Booking confirmation details are: ");
  	    	        System.out.println("Booking Id is  : " + rs3.getInt(1));
  	    	        System.out.println("Customer Id is : " + cust_id);
  	    	        System.out.println("Hotel Name     : " + hotel_name);
  	    	        System.out.println("Location       : " + location);
  	    	        System.out.println("Total cost     : " + cost + "dollors");
  	    	        System.out.println("Staying days   : " + days);
  	    	        booking_id = rs3.getInt(1);
  	    	    }
  	      }  
    	updateHotelandRoom(con, roomId, booking_id, hotel_name, location, roomCount-1);
		System.out.println("Booking confirmed");
	}
	
//	This function updates the Login table for a new customer/guest user details while booking the hotel room
	public static void updateLoginTable() throws SQLException {
		PreparedStatement updateEXP = con.prepareStatement("update `Login` set cust_id = ?  where `username` = ?");
		 updateEXP.setInt(1, cust_id);
		 updateEXP.setString(2, username);
		 int updateEXP_done = updateEXP.executeUpdate();
		 System.out.println("Login table updated " + String.valueOf(updateEXP_done));
	}
	
//	This function updates hotel and room details accordingly while a guest user is booking the hotel room 
	public static void updateHotelandRoom(Connection con, int roomId, int bookingId, String hotel_name, String location, int roomCount) throws SQLException {
		 PreparedStatement updateEXP = con.prepareStatement("update `room` set status = False, `booking_id` = ? where `room_id` = ?");
		 updateEXP.setInt(1, bookingId);
		 updateEXP.setInt(2, roomId);
		 int updateEXP_done = updateEXP.executeUpdate();
		 System.out.println(String.valueOf(updateEXP_done));
		 if(roomCount == 0) {
			 PreparedStatement updateHotelEXP = con.prepareStatement("update `hotel` set rooms_available = False where `hotel_name` = ? and `location` = ?");
			 updateHotelEXP.setString(1, hotel_name);
			 updateHotelEXP.setString(2, location);
			 int updateEXP_done1 = updateHotelEXP.executeUpdate();
			 System.out.println(String.valueOf(updateEXP_done1));
		 } else {
			 System.out.println("There are still rooms available in the hotel in the selected locations therefore no change in the hotel table");
		 }
	}
	
//	This function validates the date format and also checks if the number of days for stay matches with start date and end date
	public static void validateDate(Connection con, String date, int comparedays, String custName, String custAddress, int custPhoneNo, String custEmail, String hotel_name, int roomNumber, int cost, String location, int roomCount, int roomId) throws ParseException, SQLException {
		String[] startEnd = date.replaceAll("\\s+","").split(",");
		Date date1=new SimpleDateFormat("dd/MM/yyyy").parse(startEnd[0]);  
		Date date2=new SimpleDateFormat("dd/MM/yyyy").parse(startEnd[1]); 
		
		if(!date1.after(date2)) {
			long difference = date2.getTime() - date1.getTime();
		    int daysBetween = (int) (difference / (1000*60*60*24));
		    if( daysBetween != comparedays) {
		    	System.out.println("Number of days for stay entered doesn't match the start and end date");
		    } else {
		    	System.out.println("Successful validation");
		    	String startQuery = new SimpleDateFormat("yyyy-MM-dd").format(date1);
				String endQuery = new SimpleDateFormat("yyyy-MM-dd").format(date2);
				insertIntoTables(con, startQuery, endQuery, comparedays, custName, custAddress, custPhoneNo, custEmail, hotel_name, roomNumber, cost, location, roomCount, roomId);
		    }
		} else {
			System.out.println("Please enter a valid end date");
		}
	}
	
//	This function searches the available hotels according to the location entered by the user
	public static void search() {
		try {
			ResultSet rs = s.executeQuery(fetchHotels(location));
			if(rs.next() == false) {
				System.out.println("There are no Hotels avaliable at " + location);
			}
			else {
				System.out.println("Available Hotels at " + location + " are");
				do {
					System.out.println(rs.getString(2));
					rooms.put(rs.getString(2).toLowerCase(), rs.getInt(1));
				} while(rs.next());
				System.out.println("Select the Hotel in which you would like to check-In:");
				hotel_name = in.nextLine();
				ResultSet rs1 = s.executeQuery(selectHotel(rooms.get(hotel_name.toLowerCase())));
				if(rs1.next() == false) {
					System.out.println("There are no Rooms avaliable at Hotel " + hotel_name);
				} 
				else {
					System.out.println("Available rooms at " + hotel_name + " are");
					do {
						roomCount += 1;
						System.out.println("Room Number " + rs1.getString(2) + " with bedcount " + rs1.getInt(3) + " and cost per day is " + rs1.getString(4));
						roomCost.put(rs1.getInt(2), rs1.getInt(4));
						roomId.put(rs1.getInt(2), rs1.getInt(1));	
					} while(rs1.next());
					 
					System.out.println("Select the room number to book the room:");
					roomNumber = Integer.parseInt(in.nextLine());
					System.out.println("For how many days you would like to book the room " + String.valueOf(roomNumber) + " for:");
					days = Integer.parseInt(in.nextLine());
					cost = roomCost.get(roomNumber)*days;
					System.out.println("Total cost would be " + String.valueOf(cost) + " dollars");
					System.out.println("Enter Yes/No to confirm the booking");
					booking = in.nextLine();
					confirmBooking(booking);
				 }
				}
		} catch (Exception e1) {
			System.out.println("NO Hotels/Rooms are currently available at the selected location");
		}		
	}
	
//	This function takes booking Id to Cancel the booking for a specific user hotel room booking at specific location 
	public static void cancelBooking(String cancelId) {
		try {
			ResultSet rs3 = s.executeQuery("select cust_id, room_id from booking where booking_id = " + cancelId);
			while(rs3.next()) {
				System.out.println("Customer Id " + rs3.getInt(1));
				System.out.println("Room Id " + rs3.getInt(2));
				updateHotelandRoomCancel(rs3.getInt(2));
				s.executeUpdate("delete from booking where booking_id =" + cancelId);
			}
		} catch (Exception e2) {
			e2.printStackTrace();
			System.out.println("No such booking id is present, please provide valid booking id");
		}
	}
	
//	This function updates the status of Hotel room and Hotel status for a valid roomId
	public static void updateHotelandRoomCancel(int roomId) {
		try {
			ResultSet rs4 = s.executeQuery("select * from room where room_id = " + roomId);
			while(rs4.next()) {
				 PreparedStatement updateEXP = con.prepareStatement("update `room` set status = True, booking_id = NULL where `room_id` = ?");
				 updateEXP.setInt(1, roomId);
				 int updateEXP_done = updateEXP.executeUpdate();
				 System.out.println(String.valueOf(updateEXP_done));
				 System.out.println("Hotel Id " +  rs4.getInt(6) );
				 ResultSet rs5 = s.executeQuery("select count(*) as updateCount from room where hotel_id = " + rs4.getInt(6) + " and status is true");
				 rs5.next();
				 System.out.println(rs5.getInt("updateCount"));
				 if(rs5.getInt("updateCount") == 0) {
					 PreparedStatement updateHotelEXP = con.prepareStatement("update `hotel` set rooms_available = False where `hotel_id` = ?");
					 updateHotelEXP.setInt(1, rs4.getInt(6));
					 int cancel = updateHotelEXP.executeUpdate();
					 System.out.println(String.valueOf(cancel));
				 } else {
					 PreparedStatement updateHotelEXP = con.prepareStatement("update `hotel` set rooms_available = True where `hotel_id` = ?");
					 updateHotelEXP.setInt(1, rs4.getInt(6));
					 int cancel = updateHotelEXP.executeUpdate();
					 System.out.println(String.valueOf(cancel));
				 }				 
			}
		} catch (Exception e2) {
			e2.printStackTrace();
			System.out.println("No such booking id is present, please provide valid booking id");
		}
	}
	
//	This function prints customer details if present, otherwise user has to provide personal details.
//	Finally prints the booking status confirmation
	public static void confirmBooking(String booking) throws SQLException {
		if (booking.equalsIgnoreCase("Yes")) {
			if(cust_id == 0) {
				System.out.println("Please enter your details to book the room");
				System.out.println("Enter your Name:");
				custName = in.nextLine();
				System.out.println("Enter your Address:");
				custAddress = in.nextLine();
				System.out.println("Enter your Phone Number:");
				custPhoneNo = Integer.parseInt(in.nextLine());
				System.out.println("Enter your Email:");
				custEmail = in.nextLine();
			} else {
				ResultSet userDetails = s.executeQuery("select * from Customer where cust_id = " + cust_id);
				userDetails.next();
				System.out.println("The customer details as per the username is");
				custName = userDetails.getString(1);
				custAddress = userDetails.getString(2);
				custPhoneNo = userDetails.getInt(3);
				custEmail = userDetails.getString(4);
				
				System.out.println("Name        : " + userDetails.getString(1));
				System.out.println("Address     : " + userDetails.getString(2));
				System.out.println("Phone Number: " + userDetails.getString(3));
				System.out.println("Email       : " + userDetails.getString(4));
			}
			System.out.println("Please enter the start date and end date in the form of (dd/MM/YYYY,dd/MM/YYYY)");
			startEndDate = in.nextLine();
			try {
				validateDate(con, startEndDate, days, custName, custAddress, custPhoneNo, custEmail, hotel_name, roomNumber, cost, location, roomCount, roomId.get(roomNumber));
			} catch (ParseException e) {
				System.out.println("Please enter the dates in correct format");
			}
		} else {
			System.out.println("Please begin the process again");
		}
	}
	
//	This function is responsible for a Admin user to add and update/remove a hotel at a specific location according to hotel Id and location 
	public static void addUpdateHotel(String option) throws Exception{
		if(option == "add") {
			String addQuery = "insert into hotel (hotel_name, rooms_available, location, rating)"
	    	        + " values (?, ?, ?, ?)";
	    	      PreparedStatement preparedStmt1 = con.prepareStatement(addQuery);
	    	      preparedStmt1.setString (1, hotel_name);
	    	      preparedStmt1.setBoolean(2, false);
	    	      preparedStmt1.setString (3, location);
	    	      preparedStmt1.setInt    (4, 1);
	    	      preparedStmt1.execute();
			System.out.println("Hotel Created successfully");
		} else if (option == "update") {
			System.out.println("Please enter the updated hotel name");
			PreparedStatement updateEXP = con.prepareStatement("update `Hotel` set `hotel_name` = ? where `hotel_id` = ?");
			 updateEXP.setString(1, in.nextLine());
			 updateEXP.setInt(2, hotelId);
			 int updateEXP_done = updateEXP.executeUpdate();
			 System.out.println("Hotel Name updated successfully" + String.valueOf(updateEXP_done));

		} else if (option == "remove" ) {
			try {
				ResultSet ifHotelRoomBooked = s.executeQuery("select count(*) as count from Room where hotel_id = " + hotelId + " and Status = false");
				ifHotelRoomBooked.next();
				 System.out.println(ifHotelRoomBooked.getInt("count"));
				 if(ifHotelRoomBooked.getInt("count") == 0) {
					 System.out.println("No Rooms are booked in this hotel, so proceeding with the delete process");
					 s.executeUpdate("delete from hotel where Lower(location) like '%" + location.toLowerCase() + "%' and Lower(hotel_name) like '%" + hotel_name.toLowerCase() + "%'" );
					 System.out.println("Hotel deleted Successfully");
				 } else {
					 System.out.println("Rooms in this hotel are booked cannot delete the hotel"); 
				 }
			} catch(Exception e) {
				System.out.println("No Rooms available in this hotel");
				s.executeUpdate("delete from hotel where Lower(location) like '%" + location.toLowerCase() + "%' and Lower(hotel_name) like '%" + hotel_name.toLowerCase() + "%'" );
				System.out.println("Hotel deleted Successfully");
			}
		}
	}
	
//	This function is responsible for a Admin user to add and update/remove a room at a specific location and hotel ID according to roomId,hotel Id and location 
	public static void addUpdateRoom(String option, int hotelid) throws Exception{
		if(option == "add") {
			System.out.println("Please enter the bed count and cost in seperate line");
			int bed_count = Integer.parseInt(in.nextLine());
			cost = Integer.parseInt(in.nextLine());
			String addQuery = "insert into Room (room_no, bed_count, cost_perhour, status, hotel_id, booking_id)"
	    	        + " values (?, ?, ?, ?, ?, ?)";
	    	      PreparedStatement preparedSt = con.prepareStatement(addQuery);
	    	      preparedSt.setInt    (1, roomNumber);
	    	      preparedSt.setInt    (2, bed_count);
	    	      preparedSt.setDouble (3, cost);
	    	      preparedSt.setBoolean(4, true);
	    	      preparedSt.setInt    (5, hotelid);
	    	      preparedSt.setInt    (6, 0);
	    	      preparedSt.execute();
			System.out.println("New Room data created");
		} else if (option == "update") {
			System.out.println("Please enter the updated cost");
			cost = Integer.parseInt(in.nextLine());
			PreparedStatement updateEXP = con.prepareStatement("update `room` set `cost_perhour` = ? where `room_id` = ? and `hotel_id` = ?");
			 updateEXP.setDouble(1, cost);
			 updateEXP.setInt(2, room_Id);
			 updateEXP.setInt(3, hotelid);
			 
			 int updateEXP_done = updateEXP.executeUpdate();
			 System.out.println("Room data updated" + String.valueOf(updateEXP_done));
		} else if (option == "remove") {
			s.executeUpdate("delete from Room where room_no = " + roomNumber + " and hotel_id = " + hotelid );
			System.out.println("Room data removed");
		}
	}

	public static void main(String[] args) throws SQLException, ParseException{
		con = DriverManager.getConnection(url);
		s = con.createStatement();
		System.out.println("Please enter the username");
		username = in.nextLine();
		try {
//			checks if the username is valid or not
			ResultSet rs0 = s.executeQuery("select * from Login where username like '" + username + "'");
			while(rs0.next()) {
				role = rs0.getString(2);
				cust_id = rs0.getInt(3);
			}
			
//			Operations for a Admin user starts from here
			if (role.equalsIgnoreCase("Admin")) {
				System.out.println("Successfully logged in as a Admin user");
				System.out.println("Please select a option to perform below selected action");
				System.out.println("1.  Add/Update a Hotel at a location");
				System.out.println("2.  Add/Update a Room in the Hotel at a specific location");
				System.out.println();
				option = in.nextLine();
				if(option.contains("1")) {
					System.out.println("Please enter the location");
					location = in.nextLine();
					ResultSet rs = s.executeQuery("select * from Hotel where Lower(location) like '%" + location.toLowerCase() +"%'");
					if(rs.next() == false) {
						System.out.println("The location is not available");
					} else {
						System.out.println("Please select a option");
						System.out.println("1.  Add a Hotel at the location");
						System.out.println("2.  Update a Hotel at the location");
						System.out.println("3.  Remove a Hotel at the location");
						String hotelOption = in.nextLine();
						if (hotelOption.contains("1")) {
							System.out.println("Please enter the Hotel name:");
							hotel_name = in.nextLine();
							addUpdateHotel("add");
						} else if (hotelOption.contains("2") || hotelOption.contains("3")) {
							System.out.println("Please enter the Hotel name:");
							hotel_name = in.nextLine();
							ResultSet getHotelId = s.executeQuery("select hotel_id from Hotel where Lower(Location) like '" + location.toLowerCase() +"' and Lower(hotel_name) like '" + hotel_name.toLowerCase() + "'" );
							if(getHotelId.next() == false) {
								System.out.println("The entered hotel name at location is not available");
							} else {
								hotelId = getHotelId.getInt(1);
								if (hotelOption.contains("2")) {
									addUpdateHotel("update");
								} else if (hotelOption.contains("3")) {
									addUpdateHotel("remove");
								}
							}
						} else {
							System.out.println("Please enter a valid option");
						}
					}
					System.out.println();
				} else if(option.contains("2")) {
					System.out.println("Please enter the location then Hotel Name");
					location = in.nextLine();
					hotel_name = in.nextLine();
					ResultSet rs = s.executeQuery("select hotel_id from Hotel where Lower(Location) like '" + location.toLowerCase() +"' and Lower(hotel_name) like '" + hotel_name.toLowerCase() + "'" );
					if(rs.next() == false) {
						System.out.println("The hotel or location is not available");
					} else {
						int hotel_id = rs.getInt(1);
						System.out.println("Please select a option");
						System.out.println("1.  Add a Room");
						System.out.println("2.  Update a Room");
						System.out.println("3.  Remove a Room");
						String roomOption = in.nextLine();
						if (roomOption.contains("1")) {
							System.out.println("Please enter the Room number:");
							roomNumber = Integer.parseInt(in.nextLine());
							addUpdateRoom("add", hotel_id);
						} else if (roomOption.contains("2") || roomOption.contains("3")) {
							System.out.println("Please enter the Room number");
							roomNumber = Integer.parseInt(in.nextLine());
							ResultSet getRoomId = s.executeQuery("select room_id from Room where room_no = " + roomNumber + " and Status = True");
							if(getRoomId.next() == false) {
								System.out.println("The room Number provided in Hotel or location is not available");
							} else {
								room_Id = getRoomId.getInt(1);
								if (roomOption.contains("2")) {
									addUpdateRoom("update",hotel_id);
								} else if (roomOption.contains("3")) {
									addUpdateRoom("remove",hotel_id);
								}
							}
						} else {
							System.out.println("Please enter a valid option");
						}
					}
				} else {
					System.out.println("Please enter a valid option");
				}
				
//			Operations for a Guest user starts from here
			} else if (role.equalsIgnoreCase("Guest")) {
				System.out.println("Successfully logged in as a Guest user");
				System.out.println("Please select the option number from the below options");
				System.out.println("1.  Search and Book a Hotel at a specified location");
				System.out.println("2.  Cancel the booked Hotel room");
				option = in.nextLine();
				if(option.contains("1")) {
					ResultSet rs = s.executeQuery("select * from hotel where rooms_available = true");
					if(rs.next() == false) {
						System.out.println("There are no available Hotels at any Location");
					} else {
						System.out.println("Available Hotels along with their locations are:");
						do {
							System.out.println(rs.getString(2) + " -- " + rs.getString(4));
						}while(rs.next()) ;
						System.out.println();
					}
					System.out.println("Enter the location: ");
			        location = in.nextLine();
			        search();
				} else if(option.contains("2")) {
					System.out.println("Cancellation selected");
					System.out.println("All the bookings done on " + username + " user");
					ResultSet bookingDetails = s.executeQuery("SELECT b.booking_id, c.cust_name, h.hotel_name, r.room_no, b.Location, b.Cost, b.StartDate, b.EndDate, b.Days from booking AS b, customer AS c, room AS r, hotel AS h WHERE (((b.room_id)=[r].[room_id]) AND ((b.cust_id)=" + cust_id + ") AND ((c.cust_id)=[b].[cust_id]) and r.hotel_id = h.hotel_id)");
					while(bookingDetails.next()) {
						System.out.println("Booking Id     : " + bookingDetails.getInt(1));
						System.out.println("Customer Name  : " + bookingDetails.getString(2));
						System.out.println("Hotel Name     : " + bookingDetails.getString(3));
						System.out.println("Room Number    : " + bookingDetails.getInt(4));
	  	    	        System.out.println("Location       : " + bookingDetails.getString(5));
	  	    	        System.out.println("Total cost     : " + bookingDetails.getInt(6) + " dollars");
	  	    	        System.out.println("Start Date     : " + bookingDetails.getString(7));
	  	    	        System.out.println("End Date       : " + bookingDetails.getString(8));
	  	    	        System.out.println("Staying days   : " + bookingDetails.getInt(9));
						System.out.println();
					}
					System.out.println("Please provide the Booking Id to cancel the Hotel room");
					String cancelId = in.nextLine();
					cancelBooking(cancelId);
					System.out.println("Cancellation is done");
				} else {
					System.out.println("Please select a proper option");
				}
			}
			System.out.println();
		} catch (Exception e) {
			System.out.println("Username entered is not valid");
		}
	}
}
