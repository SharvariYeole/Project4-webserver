FROM tomee:9.1.2-jre17-alpine-webprofile
# Use Tomcat 10, which supports Servlets 5

# This limits the amount of memory used so that free-tier Heroku limits are not exceeded
ENV JAVA_OPTS="-Xmx300m"

# Expose port 8080 when running on localhost
EXPOSE 8080

# Copy in our ROOT.war to the right place in the container
COPY ROOT.war /usr/local/tomcat/webapps/

# LOCALHOST:  Run catalina in the container
# Should map localhost:8080 to this app
CMD ["catalina.sh", "run"]