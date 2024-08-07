import java.util.Objects;

public class Weather {

    /*
    {
      "city": "New York",
      "temperature": "10.34"
    }
    */

    public String city;
    public Double temperature;

    public Weather() {}

    public Weather(String city, String temperature) {
        this.city = city;
        this.temperature = Double.valueOf(temperature);
    }

    @Override
    public String toString() {
        return "Weather{" +
                "city='" + city + '\'' +
                ", temperature=" + temperature +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Weather weather = (Weather) o;
        return Objects.equals(city, weather.city) &&
                Objects.equals(temperature, weather.temperature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, temperature);
    }
}
