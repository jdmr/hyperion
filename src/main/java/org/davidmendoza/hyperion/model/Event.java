/*
 * The MIT License
 *
 * Copyright 2014 J. David Mendoza <jdmendozar@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.davidmendoza.hyperion.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import org.davidmendoza.hyperion.utils.Constants;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author J. David Mendoza <jdmendozar@gmail.com>
 */
@Entity
@Table(name = "events")
@NamedQueries({
    @NamedQuery(name = "findEventByCode", query = "select e from Event e where e.code = :code")
})
public class Event implements Serializable {
    
    @Id
    private String id;
    @NotBlank
    @Column(nullable = false, unique = true)
    private String code;
    @NotBlank
    @Column(nullable = false)
    private String name;
    private String description;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "date_", nullable = false)
    @DateTimeFormat(pattern = "MM/dd/yyyy HH:mm zzz")
    private Date date = new Date();
    @Transient
    @DateTimeFormat(pattern = "MM/dd/yyyy")
    private Date startDate = new Date();
    @Transient
    private String time;
    @Transient
    private String hour;
    @Transient
    private String minutes;
    @Transient
    private String ampm;
    @Transient
    private String timeZone;
    private String street;
    private String city;
    @Column(name = "state_")
    private String state;
    private String zip;
    private Integer seats = 0;
    @NotBlank
    @Column(nullable = false)
    private String hostName;
    @NotBlank
    @Column(nullable = false)
    private String hostPhone;
    @NotBlank
    @Email
    @Column(nullable = false)
    private String hostEmail;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date dateCreated;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date lastUpdated;
    @ManyToOne(optional = false)
    private User user;
    @Column(length = 32)
    private String contentType;
    @Column(length = 64)
    private String imageName;
    private Long imageSize;
    private byte[] imageData;
    @Transient
    private MultipartFile imageFile;
    @Column(length = 32)
    private String status = Constants.NEW;
    
    public Event() {}

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * 
     * @return the hour 
     */
    public String getHour() {
        return hour;
    }

    /**
     * 
     * @param hour the hour to set
     */
    public void setHour(String hour) {
        this.hour = hour;
    }

    /**
     * 
     * @return the minutes
     */
    public String getMinutes() {
        return minutes;
    }

    /**
     * 
     * @param minutes the minutes to set
     */
    public void setMinutes(String minutes) {
        this.minutes = minutes;
    }

    /**
     * 
     * @return am or pm
     */
    public String getAmpm() {
        return ampm;
    }

    /**
     * 
     * @param ampm the am or pm to set
     */
    public void setAmpm(String ampm) {
        this.ampm = ampm;
    }
    
    /**
     * @return the timeZone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone the timeZone to set
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * @param street the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the zip
     */
    public String getZip() {
        return zip;
    }

    /**
     * @param zip the zip to set
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * @return the seats
     */
    public Integer getSeats() {
        return seats;
    }

    /**
     * @param seats the seats to set
     */
    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return the hostPhone
     */
    public String getHostPhone() {
        return hostPhone;
    }

    /**
     * @param hostPhone the hostPhone to set
     */
    public void setHostPhone(String hostPhone) {
        this.hostPhone = hostPhone;
    }

    /**
     * @return the hostEmail
     */
    public String getHostEmail() {
        return hostEmail;
    }

    /**
     * @param hostEmail the hostEmail to set
     */
    public void setHostEmail(String hostEmail) {
        this.hostEmail = hostEmail;
    }

    /**
     * @return the dateCreated
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return the lastUpdated
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * @param lastUpdated the lastUpdated to set
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the imageName
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * @param imageName the imageName to set
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * @return the imageSize
     */
    public Long getImageSize() {
        return imageSize;
    }

    /**
     * @param imageSize the imageSize to set
     */
    public void setImageSize(Long imageSize) {
        this.imageSize = imageSize;
    }

    /**
     * @return the imageData
     */
    public byte[] getImageData() {
        return imageData;
    }

    /**
     * @param imageData the imageData to set
     */
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    /**
     * @return the imageFile
     */
    public MultipartFile getImageFile() {
        return imageFile;
    }

    /**
     * @param imageFile the imageFile to set
     */
    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Event other = (Event) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Event{" + "id=" + id + ", code=" + code + ", name=" + name + ", description=" + description + ", date=" + date + ", startDate=" + startDate + ", time=" + time + ", timeZone=" + timeZone + ", street=" + street + ", city=" + city + ", state=" + state + ", zip=" + zip + ", seats=" + seats + ", hostName=" + hostName + ", hostPhone=" + hostPhone + ", hostEmail=" + hostEmail + ", dateCreated=" + dateCreated + ", lastUpdated=" + lastUpdated + ", user=" + user + ", contentType=" + contentType + ", imageName=" + imageName + ", imageSize=" + imageSize + ", status=" + status + '}';
    }
    
}
