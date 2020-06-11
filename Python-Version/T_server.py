import socketserver
import config


class MyTCPHandler(socketserver.BaseRequestHandler):
    def handle(self):
        while True:
            # self.request is the TCP socket connected to the client
            self.data = self.request.recv(1024).strip()
            print(str(self.data, encoding='utf-8'))
            if str(self.data, encoding='utf-8') == 'login':
                config.clients.append(self)
                for i in config.clients:
                    i.request.sendall(
                        bytes(self.client_address[0] + '连接到服务器，当前连接服务器总数量为' + str(len(config.clients)) + '\n',
                              encoding='utf-8'))
            elif self.data != 'logout':
                print(self.client_address[0] + '发送消息:' + str(self.data))
                # just send back the same data, but upper-cased
                for i in config.clients:
                    i.request.sendall(bytes(self.client_address[0] + '发送消息:' + str(self.data, encoding='utf-8') + '\n',
                                            encoding='utf-8'))
            else:
                config.clients.remove(self)


if __name__ == "__main__":
    HOST, PORT = "localhost", config.ip
    # server = socketserver.TCPServer((HOST, PORT), MyTCPHandler)  # 单线程连接
    server = socketserver.ThreadingTCPServer((HOST, PORT), MyTCPHandler)  # 多线程连接
    server.serve_forever()
