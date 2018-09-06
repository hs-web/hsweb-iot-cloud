FROM node
RUN mkdir /app
COPY mqtt-emulator.js /app/
COPY package.json /app/
COPY progress-bar.js /app/
COPY data /app/data
WORKDIR /app
RUN npm install
ENTRYPOINT ["node","mqtt-emulator.js"]