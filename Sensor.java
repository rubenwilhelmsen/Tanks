// Används ännu inte.

/*
class Sensor {

	public float[] readValues() {
		return null;
	}


	public SensorReading readValue() {
		return new SensorReading();
	}

	public float readValue(int index) {
		return readValues()[index];
	}

	
	protected float getReadingAfterNoise(float reading, float noise) {
		float addedNoise = (float) Math.random() * noise - noise / 2f;
		return reading + addedNoise;
	}
	
	public class SensorReading {
		float distance;
		float heading;
		Sprite obj;

		SensorReading() {
			obj = null;
			distance = 0F;
			heading = 0F;
		}

		SensorReading(Sprite _obj, float _distance, float _heading) {
			obj = _obj;
			distance = _distance;
			heading = _heading;
		}

		Sprite obj() {
			return obj;
		}

		float getHeading() {
			return heading;
		}

		float distance() {
			return distance;
		}

	}
}*/