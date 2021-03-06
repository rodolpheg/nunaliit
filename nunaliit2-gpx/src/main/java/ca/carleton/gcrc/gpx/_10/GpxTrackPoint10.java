package ca.carleton.gcrc.gpx._10;

import java.math.BigDecimal;
import java.util.Date;

import com.topografix.gpx._1._0.Gpx.Trk.Trkseg.Trkpt;

import ca.carleton.gcrc.gpx.GpxPoint;

public class GpxTrackPoint10 implements GpxPoint {

	private Trkpt p;
	
	public GpxTrackPoint10(Trkpt p) {
		this.p = p;
	}

	@Override
	public String getName() {
		return p.getName();
	}

	@Override
	public String getDescription() {
		return p.getDesc();
	}

	@Override
	public BigDecimal getLat() {
		return p.getLat();
	}

	@Override
	public BigDecimal getLong() {
		return p.getLon();
	}

	@Override
	public BigDecimal getElevation() {
		return p.getEle();
	}

	@Override
	public Date getTime() {
		Date result = null;
		if( null != p.getTime() ) {
			result = p.getTime().toGregorianCalendar().getTime();
		}
		return result;
	}

}
