import socket
import config

client = socket.socket()

client.connect(('localhost', config.ip))
client.send('login'.encode('utf-8'))
upData = client.recv(1024)
print(upData.decode())

while True:
    msg = input().strip()
    if msg == 'logout':
        client.close()
        break
    elif not msg:
        continue
    else:
        client.send(msg.encode('utf-8'))
        upData = client.recv(1024)
        print(upData.decode())
