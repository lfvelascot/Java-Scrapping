package hitch.test.tect;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import java.sql.*;

public class Application {
	private static String url = "https://www.ktronix.com/computadores-tablet/computadores-portatiles/c/BI_104_KTRON";
	private static List<String> productsURL = new ArrayList<>();
	private static final String urlDB = "jdbc:mysql://localhost:3306/katronix";
	private static final String userDB = "ukatronix";
	private static final String passwordDB = "p4zzW0Rd";
	private static Connection conn = null;

	public static void main(String[] args) throws SQLException {
		conn = DriverManager.getConnection(urlDB, userDB, passwordDB);
		getProductos();
		if (!productsURL.isEmpty()) {
			productsURL.subList(0, 5).forEach(u -> getData(u));
			getDataDB();
		}
		conn.close();
	}

	private static void getProductos() {
		WebDriver driver = new ChromeDriver();
		driver.get(url);
		driver.findElements(By.className("js-algolia-product-title"))
				.forEach(e -> productsURL.add(e.getAttribute("href")));
		driver.quit();
	}

	private static void getData(String producturl) {
		WebDriver driver = new ChromeDriver();
		driver.get(producturl);
		loadData(new Product(driver.findElement(By.className("new-container__header__title")).getText(),
				driver.findElement(By.className("new-container__header__code")).getText().replaceFirst("Código: ", ""),
				Integer.parseInt(driver.findElement(By.id("js-original_price")).getText().replace(".", "")
						.replace("$", "").replaceFirst("\\nHoy", ""))));
		driver.quit();
	}

	private static void loadData(Product p) {
		try {
			conn.createStatement().executeUpdate("INSERT INTO products (name, code, price) " + "VALUES ('" + p.getName()
					+ "', '" + p.getCode() + "','" + String.valueOf(p.getPrice()) + "')");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static void getDataDB() throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM products;");
		System.out.println("DATOS EXISTENTES");
		while (rs.next()) {
			System.out.format("- %s, %s, %s, %s \n", rs.getInt("id"), rs.getString("name"), rs.getString("code"),
					rs.getInt("price"));
		}
		st.close();
	}

}
