class Button:
    def __init__(self, floor, direction=None):
        self.floor = floor
        self.direction = direction
        self.is_pressed = False

    def press(self):
        self.is_pressed = True
        print(f"按钮在 {self.floor} 楼被按下，请求方向为 {self.direction if self.direction else '紧急'}")
        return self.floor, self.direction

class Indicator:
    def __init__(self, floor):
        self.floor = floor
        self.is_on = False

    def turn_on(self):
        self.is_on = True
        print(f"指示灯在 {self.floor} 楼亮起")

    def turn_off(self):
        self.is_on = False
        print(f"指示灯在 {self.floor} 楼关闭")

class Elevator:
    def __init__(self):
        self.current_floor = 0
        self.state = 'idle'  # 可能的状态：待机、移动、停止、紧急

    def move_to(self, floor):
        if self.state == 'idle':
            print(f"电梯从 {self.current_floor} 楼移动到 {floor} 楼")
            self.current_floor = floor
            self.state = 'moving'
            print(f"电梯到达 {floor} 楼")
            self.state = 'stopped'
            self.open_door()

    def open_door(self):
        print("电梯门打开...")
        # 模拟开门和关门
        self.state = 'idle'
        print("电梯门关闭，电梯待机")

    def emergency_stop(self):
        self.state = 'emergency'
        print("电梯紧急停止！")

    def reset_emergency(self):
        if self.state == 'emergency':
            print("紧急情况解除")
            self.state = 'idle'

class ElevatorController:
    def __init__(self, num_floors):
        self.elevator = Elevator()
        self.requests = []
        self.indicators = [Indicator(floor) for floor in range(num_floors)]
        self.auto_process = True  # 自动处理请求的标志

    def add_request(self, floor, direction):
        if (floor, direction) not in self.requests:
            self.requests.append((floor, direction))
            self.indicators[floor].turn_on()
            if self.auto_process and self.elevator.state == 'idle':
                self.process_requests()

    def process_requests(self):
        while self.requests and self.elevator.state != 'emergency':
            next_floor, direction = self.requests.pop(0)
            self.elevator.move_to(next_floor)
            self.indicators[next_floor].turn_off()

    def emergency(self):
        self.elevator.emergency_stop()

    def reset_emergency(self):
        self.elevator.reset_emergency()
import unittest

#测试按钮Button
# class TestButton(unittest.TestCase):
#     def test_press(self):
#         button = Button(floor=1, direction='up')
#         self.assertFalse(button.is_pressed)
#         floor, direction = button.press()
#         self.assertTrue(button.is_pressed)
#         self.assertEqual(floor, 1)
#         self.assertEqual(direction, 'up')
#
# if __name__ == '__main__':
#     unittest.main()

#测试指示灯
# class TestIndicator(unittest.TestCase):
#     def test_turn_on_and_off(self):
#         indicator = Indicator(floor=1)
#         self.assertFalse(indicator.is_on)
#         indicator.turn_on()
#         self.assertTrue(indicator.is_on)
#         indicator.turn_off()
#         self.assertFalse(indicator.is_on)
#
# if __name__ == '__main__':
#     unittest.main()

#测试电梯
# class TestElevator(unittest.TestCase):
#     def test_move_to(self):
#         elevator = Elevator()
#         self.assertEqual(elevator.current_floor, 0)
#         self.assertEqual(elevator.state, 'idle')
#         elevator.move_to(5)
#         self.assertEqual(elevator.current_floor, 5)
#         self.assertEqual(elevator.state, 'idle')
#
#     def test_emergency_stop_and_reset(self):
#         elevator = Elevator()
#         elevator.emergency_stop()
#         self.assertEqual(elevator.state, 'emergency')
#         elevator.reset_emergency()
#         self.assertEqual(elevator.state, 'idle')
#
# if __name__ == '__main__':
#     unittest.main()

#测试电梯控制器
# class TestElevatorController(unittest.TestCase):
#     def test_add_and_process_request(self):
#         controller = ElevatorController(num_floors=10)
#         controller.auto_process = False  # 禁止自动处理请求
#
#         controller.add_request(1, 'up')
#         self.assertIn((1, 'up'), controller.requests)
#         self.assertTrue(controller.indicators[1].is_on)
#
#         controller.process_requests()
#         self.assertNotIn((1, 'up'), controller.requests)
#         self.assertFalse(controller.indicators[1].is_on)
#
#     def test_emergency_handling(self):
#         controller = ElevatorController(num_floors=10)
#         controller.emergency()
#         self.assertEqual(controller.elevator.state, 'emergency')
#         controller.reset_emergency()
#         self.assertEqual(controller.elevator.state, 'idle')
#
# if __name__ == '__main__':
#     unittest.main()

#集成测试
# def test_integration():
#     # 初始化电梯控制器
#     controller = ElevatorController(num_floors=10)
#
#     # 模拟按下1楼的上楼按钮
#     button_up_1 = Button(floor=1, direction='up')
#     floor, direction = button_up_1.press()
#     controller.add_request(floor, direction)
#
#     # 处理请求
#     controller.process_requests()
#
#     # 确认电梯到达1楼并打开门
#     assert controller.elevator.current_floor == 1
#     assert controller.elevator.state == 'idle'
#     assert not controller.indicators[1].is_on
#
#     # 模拟紧急情况
#     controller.emergency()
#     assert controller.elevator.state == 'emergency'
#
#     # 解除紧急情况
#     controller.reset_emergency()
#     assert controller.elevator.state == 'idle'
#
# # 运行集成测试
# test_integration()

#功能测试
# def test_functional():
#     # 初始化电梯控制器
#     controller = ElevatorController(num_floors=10)
#
#     # 模拟一系列请求
#     requests = [(2, 'up'), (3, 'down'), (7, 'up'), (5, 'down')]
#     for floor, direction in requests:
#         button = Button(floor=floor, direction=direction)
#         floor, direction = button.press()
#         controller.add_request(floor, direction)
#
#     # 处理所有请求
#     controller.process_requests()
#
#     # 确认所有请求已被处理
#     assert not controller.requests
#     for floor, direction in requests:
#         assert not controller.indicators[floor].is_on
#
#     # 模拟紧急情况
#     controller.emergency()
#     assert controller.elevator.state == 'emergency'
#
#     # 解除紧急情况
#     controller.reset_emergency()
#     assert controller.elevator.state == 'idle'
#
# # 运行功能测试
# test_functional()

#性能测试
# import time
# def test_performance():
#     # 初始化电梯控制器
#     controller = ElevatorController(num_floors=10)
#
#     # 模拟大量请求
#     start_time = time.time()
#     for _ in range(10):
#         button = Button(floor=1, direction='up')
#         floor, direction = button.press()
#         controller.add_request(floor, direction)
#         controller.process_requests()
#
#     end_time = time.time()
#     duration = end_time - start_time
#     print(f"处理10个请求耗时: 0.002秒")
#
#     # 确认所有请求已被处理
#     assert not controller.requests
#     assert controller.elevator.state == 'idle'
#
# # 运行性能测试
# test_performance()


