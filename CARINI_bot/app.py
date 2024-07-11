from flask import Flask, request
from flask import send_from_directory
import sys
from flask import jsonify
import numpy as np
import pandas as pd
app = Flask(__name__)

import MySQLdb

import os


@app.route('/static/<path:filename>')
def serve_static(filename):
    return send_from_directory('static', filename)
    



@app.route('/api/welcome', methods=['POST'])
def welcome():
    # 카카오 챗봇에서 오는 요청 처리
    body = request.get_json()
    
    # 웰컴 메시지 생성
    responseBody = {
        "version": "2.0",
        "template": {
            "outputs": [
                {
                    "basicCard": {
                        "description": 
                        '''
반갑습니다 고객님!
CARINI채널을 추가해 주셔서 감사합니다!

저희 CARINI는 적은 자동차 경험에 도움을 드리고 자동차 선택의 부담을 덜어드리기 위해 탄생했으며, 고객님의 요구에 맞는 자동차를 추천해드리는 기능의 챗봇입니다.

CARINI가 복잡한 자동차 시장에서 섬세한 기준으로까다롭게 선별해놓겠습니다!
고생은 CARINI가 할게요! 고객님은 편하게 원하시는 니즈만 말씀해주세요~
''',
                        "thumbnail": {
                            "imageUrl": "https://carini-zuwal.run.goorm.site/static/welcome.jpg"
                        },
                        "buttons": [
                            {
                                "action": "message",
                                "label": "카린이 소개",
                                "messageText": "카린이 소개"
                            },
                            {
                                "action": "message",
                                "label": "카린이 시작하기",
                                "messageText": "카린이 시작하기"
                            }
                        ]
                    }
                }
            ]
        }
    }
    return jsonify(responseBody)
data = {"가격" : "가격 선택안함", "크기" : "크기 선택안함", "연료":"연료 선택안함","정렬":""}
@app.route('/api/price_category', methods=['POST'])
def price_category():
    body = request.get_json()
    
    # 선택한 버튼의 값 가져오기
    data["가격"] = "가격 선택안함"
    data["크기"] = "크기 선택안함"
    data["연료"] = "연료 선택안함"
    data["정렬"] = ""
    # 응답 생성
    responseBody = {
        "version": "2.0",
        "template": {
        "outputs" : [
        {
        "basicCard": {
          "title": "💸가격을 선택해주세요!",
          "description": '''
          자동차 가격 필터링을 통해 목표 예산을 결정하는 것은 자동차 구매의 첫걸음입니다!
여행을 떠나기 전 중요한 첫단추를 끼워볼까요!? 🙂🙂

제게 예산 범위를 알려주시면, 고객님만을 위해 특별히 맞춤화된 옵션을 마련해드리겠습니다!
          ''',
            "thumbnail": {
                            "imageUrl": "https://carini-zuwal.run.goorm.site/static/price1.png"
                        },
          "buttons": [
            {
              "action": "message",
              "label": "5천만원 미만",
              "messageText": "5000만원 미만"
            },
            {
              "action": "message",
              "label": "5천만원이상 1억원미만",
              "messageText": "5000만원이상 1억원미만"
            },
              {
              "action": "message",
              "label": "1억원 이상",
              "messageText": "1억원 이상"
            }
          ]
        }
      }
    ]
  }
}
    return responseBody

@app.route('/api/size_category_top', methods=['POST'])
def size_category_top():
    # 요청 데이터 가져오기
    body = request.get_json()
    # 선택한 버튼의 값 가져오기
    
    data["가격"] = body["userRequest"]["utterance"]
    # 응답 생성
    responseBody = {
        "version": "2.0",
        "template": {
        "outputs" : [
        {
        "basicCard": {
          "title": "📐크기를 선택해주세요!",
          "description": '''
          차량의 크기는 주행 경험을 결정적인 영향을 줍니다!! 
다양한 크기의 차량 중 우리 CARINI고객님만의 취향을 골라주세요~!😉😉

고객님의 라이프스타일에 가장 적합한 차량 크기가 무엇인지 알려주시면,
최고의 차량을 찾는 여정의 큰 발걸음이 됩니다!
          ''',
            "thumbnail": {
                            "imageUrl": "https://carini-zuwal.run.goorm.site/static/size.png"
                        },
          "buttons": [
            {
              "action": "message",
              "label": "SMALL",
              "messageText": "SMALL"
            },
            {
              "action": "message",
              "label": "MEDIUM",
              "messageText": "MEDIUM"
            },
              {
              "action": "message",
              "label": "BIG",
              "messageText": "BIG"
            }
          ]
        }
      }
    ]
  }
}
    
    return responseBody

@app.route('/api/size_category', methods=['POST'])
def size_category():
    # 요청 데이터 가져오기
    body = request.get_json()
    if body["userRequest"]["utterance"]=="SMALL":
        responseBody = {
        "version": "2.0",
        "template": {
        "outputs" : [
        {
        "basicCard": {
          "title": "🚙소형 종류를 선택해 주세요!!",
          "description": '''
          소형 차량은 작지만 강력한 매력을 지니고 있습니다!
도심 주행에 최적화되어 협소한 주차공간에서도 충분히 자유롭게 움직일 수 있죠~!
실용적인 내부공간은 포근하고 안락한 탑승 경험을 제공합니다.😋😋

소형 차량은 작다고 생각하실 수 있지만,
놀라운 품질과 성능을 지닌 의외로 고급스러운 선택지입니다!
          ''',
            "thumbnail": {
                            "imageUrl": "https://search.pstatic.net/common?quality=75&direct=true&src=https%3A%2F%2Fimgauto-phinf.pstatic.net%2F20230911_242%2Fauto_16944065557452uMcS_PNG%2F20230911132909_PJFpFh66.png"
                        },
          "buttons": [
                {
                  "action": "message",
                  "label": "경형",
                  "messageText": "경형"
                },
                {
                  "action": "message",
                  "label": "소형",
                  "messageText": "소형"
                },
                  ]
                }
              }
            ]
          }
        }
    elif body["userRequest"]["utterance"]=="MEDIUM":
        responseBody = {
        "version": "2.0",
        "template": {
        "outputs" : [
        {
        "basicCard": {
          "title": "🚐중형 종류를 선택해 주세요!!",
          "description": '''
          중형 차량은 우아함과 성능을 완벽하게 조화시킨 선택지입니다!
적당히 넓은 실내 공간은 고객님들에게 편안한 탑승 경험을 선사하며, 
충분하게 마련된 수납공간은 여행 중 필요한 물건을 부족하지 않게 수용할 수 있죠~!😊😊

중형 차량은 고급스러움과 편안함을 동시에 추구하는 고객님의 요구를 만족시켜드릴 수 있습니다!!
            ''',
            "thumbnail": {
                            "imageUrl": "https://search.pstatic.net/common?quality=75&direct=true&src=https%3A%2F%2Fimgauto-phinf.pstatic.net%2F20240507_124%2Fauto_1715044554731WMy1L_PNG%2F20240507101553_CosXTFQd.png"
                        },
          "buttons": [
                {
                  "action": "message",
                  "label": "준중형",
                  "messageText": "준중형"
                },
                {
                  "action": "message",
                  "label": "중형",
                  "messageText": "중형"
                },
                  ]
                }
              }
            ]
          }
        }
    elif body["userRequest"]["utterance"]=="BIG":
        responseBody = {
        "version": "2.0",
        "template": {
        "outputs" : [
        {
        "basicCard": {
          "title": "🚎대형 종류를 선택해 주세요!!",
          "description": '''
          대형 차량은 단연 압도적인 품격과 기능을 자랑하죠~
넓은 실내 공간과 높은 안전성은 기본으로 제공하면서도 우아하고 세련된 디자인까지 갖추고 있습니다~! 
튼튼한 골격에 멈추지 않고 고급스러운 내부를 통해 고객님처럼 높은 품격을 보여준답니다~😌😌

대형 차량은 높은 안정성과 감출 수 없는 우아함을 추구하는 고객님의 요구를 완벽히 충족시킬 수 있는 최상의 선택지입니다!
          ''',
            "thumbnail": {
                            "imageUrl": "https://search.pstatic.net/common?quality=75&direct=true&src=https%3A%2F%2Fimgauto-phinf.pstatic.net%2F20230404_256%2Fauto_16805976927634AIkO_PNG%2F20230404174120_k9GZqf85.png"
                        },
          "buttons": [
                {
                  "action": "message",
                  "label": "준대형",
                  "messageText": "준대형"
                },
                {
                  "action": "message",
                  "label": "대형",
                  "messageText": "대형"
                },
                  ]
                }
              }
            ]
          }
        }
    return responseBody

@app.route('/api/oil_category', methods=['POST'])
def oil_category():
    body = request.get_json()
    # 선택한 버튼의 값 가져오기
    data["크기"] = body["userRequest"]["utterance"]
    print(data)
    # 응답 생성
    responseBody = {
    "version": "2.0",
    "template": {
        "outputs": [
            {
                "carousel": {
                    "type": "basicCard",
                    "items": [
                        {
                            "title": "⛽연료를 선택해주세요!!",
                            "description": '''
다양한 연료 유형 중 선호하시는 선택지를 말씀해주세요 CARINI가 원하시는 차량을 찾아드릴게요!😗😗
                            ''',
                            "thumbnail": {
                                "imageUrl": "https://carini-zuwal.run.goorm.site/static/fuel.jpg"
                            },
                        },
                        {
                            "title": "가솔린",
                            "description": "가솔린(휘발유)은 가장 대중적인 자동차 연료입니다. 출력이 우수하며, 엔진의 잔진동이 적습니다.",
                            "thumbnail": {
                                "imageUrl": "https://carini-zuwal.run.goorm.site/static/gasoline2.png"
                            },
                            "buttons": [
                                {
                                    "action": "message",
                                    "label": "가솔린",
                                    "messageText": "가솔린"
                                }
                            ]
                        },
                        {
                            "title": "디젤",
                            "description": "디젤(경유)는 낮은 엔진 회전수에서도 강한 힘을 제공하며, 가솔린에 비해 연비가 좋습니다.",
                            "thumbnail": {
                                "imageUrl": "https://carini-zuwal.run.goorm.site/static/diesel2.png"
                            },
                            "buttons": [
                                {
                                    "action": "message",
                                    "label": "디젤",
                                    "messageText": "디젤"
                                }
                            ]
                        },
                        {
                            "title": "전기",
                            "description": "전기차(EV)는 대용량 배터리를 동력원으로 이용합니다. 모터로 주행하기에 정숙성과 가속력이 우수합니다.",
                            "thumbnail": {
                                "imageUrl": "https://carini-zuwal.run.goorm.site/static/electric2.png"
                            },
                            "buttons": [
                                {
                                    "action": "message",
                                    "label": "전기",
                                    "messageText": "전기"
                                }
                            ]
                        },
                        {
                            "title": "하이브리드",
                            "description": "하이브리드는 두가지 동력원을 사용합니다. 모터가 엔진을 보조하여 저속, 고속주행에 따라 연비를 개선합니다.",
                            "thumbnail": {
                                "imageUrl": "https://carini-zuwal.run.goorm.site/static/hybrid.jpg"
                            },
                            "buttons": [
                                {
                                    "action": "message",
                                    "label": "하이브리드",
                                    "messageText": "하이브리드"
                                }
                            ]
                        },
                        {
                            "title": "LPG",
                            "description": "LPG(액화석유가스)는 가연성 가스의 연료이며, 석유연료에 비해 가격이 매우 저렴합니다.",
                            "thumbnail": {
                                "imageUrl": "https://carini-zuwal.run.goorm.site/static/lpg2.png"
                            },
                            "buttons": [
                                {
                                    "action": "message",
                                    "label": "LPG",
                                    "messageText": "LPG"
                                }
                            ]
                        }
                    ]
                }
            }
        ]
    }
}
    return responseBody

@app.route('/api/sort', methods=['POST'])
def sort():
    body = request.get_json()
    data["연료"] = body["userRequest"]["utterance"]
    print(data)
    # 응답 생성
    responseBody = {
        "version": "2.0",
        "template": {
        "outputs" : [
        {
        "basicCard": {
          "title": "🫧정렬 순서를 선택해주세요!!",
          "description": 
          '''
          고객님이 원하시는 주행, 품질, 디자인을 고려하여 
주행의 편의성과 안전성, 브랜드의 신뢰성과 내구성, 마지막으로 고객님의 만족도를 높여드리겠습니다!😏😏

고객님께서 보내주신 소중한 의견을 바탕으로 다양한 선택지들을 종합하여 고르신 기준에 부합하는 차량을 정렬하여 제공해 드리겠습니다!
          ''',
            "thumbnail": {
                            "imageUrl": "https://carini-zuwal.run.goorm.site/static/sort.png"
                        },
          "buttons": [
            {      
              "action": "message",
              "label": "주행",
              "messageText": "주행"
            },
              {
              "action": "message",
              "label": "디자인",
              "messageText": "디자인"
            },
              {
              "action": "message",
              "label": "거주성",
              "messageText": "거주성"
            }
          ]
        }
      }
    ]
  }
}

    return responseBody

file_path = './data/car_data_onlynum1.csv'
if os.path.exists(file_path):
    print("파일이 존재합니다.")
else:
    print("파일이 존재하지 않습니다.")
try:
    df = pd.read_csv(file_path, delimiter=',', encoding='utf-8')
    print("파일을 성공적으로 읽었습니다.")
except FileNotFoundError:
    print("파일을 찾을 수 없습니다.")
except pd.errors.ParserError as e:
    print("파싱 중 오류가 발생했습니다:", e)
except Exception as e:
    print("예상치 못한 오류가 발생했습니다:", e)

values=""
@app.route('/api/result', methods=['POST'])
def result():
    body = request.get_json()
    data["정렬"] = body["userRequest"]["utterance"]
    print("-==============")
    print(data)
    #print(body)
    
    # user_id = body['userRequest']['user']['id']
    # utterance = body['userRequest']['utterance']
    
    # # 사용자 정보 가져오기
    # user_properties = body['userRequest']['user']['properties']
    # print(user_properties)
    
    # # 사용자 이름 가져오기
    # user_name = user_properties.get('nickname', 'Unknown')
    
    # # 사용자 전화번호 가져오기
    # user_phone = user_properties.get('phone_number', 'Unknown')
    
    # # 사용자 아이디 가져오기
    # user_id = user_properties.get('appUserId', 'Unknown')
    # res123ponse = f"사용자 이름: {user_name}, 전화번호: {user_phone}, 아이디: {user_id}"
    # print(res123ponse)
    
    # 가격 필터링
    if data["가격"] == "5000만원 미만":
        df_price = df[df["가격(최저가)"] < 5000]
    elif data["가격"] == "5000만원이상 1억원미만":
        df_price = df[(df["가격(최저가)"] >= 5000) & (df["가격(최저가)"] < 10000)]
    elif data["가격"] == "1억원 이상":
        df_price = df[df["가격(최저가)"] >= 10000]
    else:
        df_price = df

    # 크기 필터링
    if data["크기"] != "크기 선택안함" or not df_price.empty:
        df_size = df_price[df_price["크기"].str.split(" ").str[0] == data["크기"]]
    else:
        df_size = df_price

    # 연료 필터링
    if data["연료"] != "연료 선택안함" or not df_size.empty:
        df_fuel = df_size[df_size["연료"].str.strip().str.split(",").apply(lambda x: data["연료"] in x)]
    else:
        df_fuel = df_size

    # 정렬 키 확인 및 정렬
    if not df_fuel.empty:
        if data["정렬"] in df_fuel.columns:
            df_sort = df_fuel.sort_values(by=[data["정렬"]], axis=0, ascending=False)
        else:
            # 기본 정렬 열을 설정 (품질)
            default_sort_column = "품질"
            df_sort = df_fuel.sort_values(by=[default_sort_column], axis=0, ascending=False)
    else:
        df_sort = df_fuel  # 비어있는 데이터프레임 그대로 반환
    
    print(df_sort)

    content = "고객님께서 보내주신 소중한 의견을 담은 최종 결과입니다! 필터링 결과는\n" + data["가격"] + "," +data["크기"] + "," + data["연료"] + "이고 정렬은" + data["정렬"] + " 순 입니다!"
    if len(df_sort)==0:
        pass
    else:
        # 1) db접속
        conn = MySQLdb.connect(host="localhost", port=3306, db='carinibot', user='root', password='12345')
        cursor = conn.cursor()
        values = (data["가격"],data["크기"],data["연료"],data["정렬"])
        check_query = "SELECT * FROM cariniBot WHERE price = %s AND size = %s AND fuel = %s AND sort = %s"
        cursor.execute(check_query, values)
        existing_row = cursor.fetchone()

        if existing_row:
            # 이미 존재하는 경우: cnt 값 증가
            update_query = "UPDATE cariniBot SET cnt = cnt + 1 WHERE price = %s AND size = %s AND fuel = %s AND sort = %s"
            cursor.execute(update_query, values)
        else:
            # 존재하지 않는 경우: 새로운 행 삽입
            insert_query = "INSERT INTO cariniBot (price, size, fuel, sort, cnt) VALUES (%s, %s, %s, %s, 1)"
            cursor.execute(insert_query, values)

        cursor.close()
        conn.commit()
        conn.close()
    list1 = []
    
    for i in range(0,len(df_sort)+1):
        if len(df_sort) == 0:
            
            responseBody = {
        "version": "2.0",
        "template": {
        "outputs" : [
        {
        "basicCard": {
          "title": "필터링 결과 없음",
          "description": '''
          필터링 결과가 존재하지 않습니다!
          
다시 시작해주세요!
            ''',
            "thumbnail": {
                            "imageUrl": "https://carini-zuwal.run.goorm.site/static/search_no.png"
                        },
          "buttons": [
                {
                  "action": "message",
                  "label": "카린이 시작하기",
                  "messageText": "카린이 시작하기"
                },
                  ]
                }
              }
            ]
          }
        }
            return responseBody
            
        elif len(df_sort) > 3:
            if i < 2:
                data1 = {}
                description= "가격 : " + str(format(df_sort.iloc[i,1], ',')) + "만원, 크기 : " + str(df_sort.iloc[i,3]) + ",\n연비 : " + df_sort.iloc[i,5]
                data1["title"] =  str(i+1)+ "순위 " + df_sort.iloc[i, 0]
                data1["description"] = description
                data1["thumbnail"] = {"imageUrl": df_sort.iloc[i, 6]}
                data1["buttons"] =  [
                                        {
                                            "action": "webLink",
                                            "label": "자세히 보기",
                                            "webLinkUrl": "https://www.carini.p-e.kr/model/getModel?carId="+str(df_sort.iloc[i,14])
                                        },
                                        {
                                            "action": "share",
                                            "label": "공유하기"
                                        }
                                    ]
            elif i == 2:
                data1 = {}
                description= "가격 : " + str(format(df_sort.iloc[i,1], ',')) + "만원, 크기 : " + str(df_sort.iloc[i,3]) + ",\n연비 : " + df_sort.iloc[i,5]
                data1["title"] =  str(i+1)+ "순위 " + df_sort.iloc[i, 0]
                data1["description"] = description
                data1["thumbnail"] = {"imageUrl": df_sort.iloc[i, 6]}
                data1["buttons"] =  [
                                        {
                                            "action": "webLink",
                                            "label": "자세히 보기",
                                            "webLinkUrl": "https://www.carini.p-e.kr/model/getModel?carId="+str(df_sort.iloc[i,14])
                                        },
                                        {
                                            "action": "share",
                                            "label": "공유하기"
                                        },
                                        {
                                            "action": "message",
                                            "label": "더보기",
                                            "messageText": "더보기"
                                        }
                                    ]
                list1.append(data1)
                break
        elif len(df_sort)<=3 and i<len(df_sort):
            
            data1 = {}
            description= "가격 : " + str(format(df_sort.iloc[i,1], ',')) + "만원, 크기 : " + str(df_sort.iloc[i,3]) + ",\n연비 : " + df_sort.iloc[i,5]
            data1["title"] =  str(i+1)+ "순위 " + df_sort.iloc[i, 0]
            data1["description"] = description
            data1["thumbnail"] = {"imageUrl": df_sort.iloc[i, 6]}
            data1["buttons"] =  [
                                    {
                                        "action": "webLink",
                                        "label": "자세히 보기",
                                        "webLinkUrl": "https://www.carini.p-e.kr/model/getModel?carId="+str(df_sort.iloc[i,14])
                                    },
                                    {
                                        "action": "share",
                                        "label": "공유하기"
                                    }
                                ]
        else:
            break
        

        
        
        list1.append(data1)
    print(list1)
    
    responseBody = {
        "version": "2.0",
        "template": {
            "outputs": [
                {
                    "basicCard": {
                        "title": "고객님께서 고른 최고의 선택지",
                        "description": content,
                        "thumbnail": {
                            "imageUrl": "https://carini-zuwal.run.goorm.site/static/result1.png"
                        },
                    }
                },
                {
                    "carousel": {
                        "type": "basicCard",
                        "items": list1
                    }
                }
            ]
        }
    }
    
    return responseBody

@app.route('/api/result_more', methods=['POST'])
def result_more():
    body = request.get_json()
    # print(body)
    
    print("===============")
    print(data)
    # 가격 필터링
    if data["가격"]=="5000만원 미만":
        filterMinPrice = "0";
        filterMaxPrice = "5000";
        df_price = df[df["가격(최저가)"]<5000]
    elif data["가격"]=="5000만원이상 1억원미만":
        filterMinPrice = "5000";
        filterMaxPrice = "10000";
        df_price = df[(df["가격(최저가)"]>=5000) & (df["가격(최저가)"]<10000)]
    elif data["가격"]=="1억원 이상":
        filterMinPrice = "10000";
        filterMaxPrice = "50000";
        df_price = df[df["가격(최저가)"]>=10000]
    else:
        df_price = df

    # 크기 필터링
    if data["크기"] != "크기 선택안함":
        filterSize=str(data["크기"]);
        df_size = df_price[df_price["크기"].str.split(" ").str[0] == data["크기"]]
    else:
        filterSize="선택안함"
        df_size = df_price

    # 연료 필터링
    if data["연료"] != "연료 선택안함":
        filterFuel=str(data["연료"]);
        df_fuel = df_size[df_size["연료"].str.strip().str.split(",").apply(lambda x: data["연료"] in x)]
    else:
        filterFuel="선택안함"
        df_fuel = df_size
    df_sort = df_fuel.sort_values(by=[data["정렬"]], axis=0, ascending=False)
    print(df_sort)
    list = []
    
    for index,i in enumerate(range(3,len(df_sort))):
        data1 = {}
        if index == 10:
            data1["title"] = "더 알아보고 싶으신가요?"
            data1["description"] = "버튼을 눌러 카린이 홈페이지와 함께하세요!!"
            data1["thumbnail"] = "https://carini-zuwal.run.goorm.site/static/search3.png"
            data1["buttons"] =  [
                                {
                                    "action": "webLink",
                                    "label": "자세히 보기",
                                    "webLinkUrl": "https://www.carini.p-e.kr/model/getModelList?searchWord=&filterMinPrice="+filterMinPrice+"&filterMaxPrice="+filterMaxPrice+"&filterSize="+filterSize+"&filterFuel="+filterFuel+"&carSort=저가순"
                                }
                            ]
            print(data1)
            list.append(data1)
            break
        
        description= "가격 : " + str(format(df_sort.iloc[i,1], ',')) + "만원, 크기 : " + str(df_sort.iloc[i,3]) + ",\n연비 : " + df_sort.iloc[i,5]
        data1["title"] =  str(i+1)+ "순위 " + df_sort.iloc[i, 0]
        data1["description"] = description
        data1["thumbnail"] = {"imageUrl": df_sort.iloc[i, 6]}
        data1["buttons"] =  [
                                {
                                    "action": "webLink",
                                    "label": "자세히 보기",
                                    "webLinkUrl": "https://www.carini.p-e.kr/model/getModel?carId="+str(df_sort.iloc[i,14])
                                },
                                {
                                    "action": "share",
                                    "label": "공유하기"
                                }
                            ]
        list.append(data1)
        
    

    responseBody = {
        "version": "2.0",
        "template": {
            "outputs": [
                {
                    "carousel": {
                        "type": "basicCard",
                        "items": list
                    }
                }
            ]
        }
    }

    return responseBody

@app.route('/api/bookmark', methods=['POST'])
def bookmark():
    body = request.get_json()

    # 1) db접속
    conn = MySQLdb.connect(host="localhost", port=3306, db='carinibot', user='root', password='12345')
    cursor = conn.cursor()

    check_query = "SELECT * FROM cariniBot ORDER BY cnt DESC"
    cursor.execute(check_query)
    all_rows = cursor.fetchall()
    print(all_rows)
    list3=[]
    
    for i in range(3):
        print(i)
        data2={}
        price = all_rows[i][0]
        size = all_rows[i][1]
        fuel = all_rows[i][2]
        sort = all_rows[i][3]
        print(fuel)
        print(sort)
        # 가격 필터링
        if price == "5000만원 미만":
            df_price = df[df["가격(최저가)"] < 5000]
        elif price_category() == "5000만원이상 1억원미만":
            df_price = df[(df["가격(최저가)"] >= 5000) & (df["가격(최저가)"] < 10000)]
        elif price == "1억원 이상":
            df_price = df[df["가격(최저가)"] >= 10000]
        else:
            df_price = df

        # 크기 필터링
        if size != "크기 선택안함" or not df_price.empty:
            df_size = df_price[df_price["크기"].str.split(" ").str[0] == size]
        else:
            df_size = df_price
        print(df_size)
        # 연료 필터링
        if fuel != "연료 선택안함" or not df_size.empty:
            df_fuel = df_size[df_size["연료"].str.strip().str.split(",").apply(lambda x: fuel in x)]
        else:
            df_fuel = df_size
        # 정렬 키 확인 및 정렬
        if not df_fuel.empty:
            df_sort = df_fuel.sort_values(by=[sort], axis=0, ascending=False)
        else:
            df_sort = df_fuel  # 비어있는 데이터프레임 그대로 반환
        
        description= "가격 : " + str(format(df_sort.iloc[0,1], ',')) + "만원, 크기 : " + str(df_sort.iloc[0,3]) + ",\n연비 : " + df_sort.iloc[0,5]
        name = df_sort.iloc[0,0]
        
        data2["title"] = str(i+1) + "순위!"
        data2["description"] = description
        data2["thumbnail"] = {"imageUrl": df_sort.iloc[0, 6]}
        data2["buttons"] = [
                                {
                                    "action": "webLink",
                                    "label": "자세히 보기",
                                    "webLinkUrl": "https://www.carini.p-e.kr/model/getModel?carId="+str(df_sort.iloc[0,14])
                                },
                                {
                                    "action": "message",
                                    "label": "어떤 기준을 거쳤을까요?",
                                    "messageText": str(i+1)+"순위 필터링"
                                }
                            ]
        list3.append(data2)
        print(list3)
    print(list3)
    responseBody = {
    "version": "2.0",
    "template": {
        "outputs": [
            {
                "carousel": {
                    "type": "basicCard",
                    "items": list3
                }
            }
        ]
    }
}

    cursor.close()
    conn.commit()
    conn.close()

    return responseBody

@app.route('/api/filter_rank', methods=['POST'])
def filter_rank():
    body = request.get_json()
    filter_rank=body["userRequest"]["utterance"]
    print(filter_rank)
    # 1) db접속
    conn = MySQLdb.connect(host="localhost", port=3306, db='carinibot', user='root', password='12345')
    cursor = conn.cursor()

    check_query = "SELECT * FROM cariniBot ORDER BY cnt DESC"
    cursor.execute(check_query)
    all_rows = cursor.fetchall()
    print(all_rows[0])
    print("+++++++++++++++")
    print(all_rows[1])
    print("+++++++++++++++")
    print(all_rows[2])
    print("+++++++++++++++")
    
    list4=[]
    for i in range(3):
        data={}
        price = all_rows[i][0]
        size = all_rows[i][1]
        fuel = all_rows[i][2]
        sort = all_rows[i][3]
        
        description= "가격 : " + price + "\n크기 : " + size + "\n연료 : " + fuel + "\n정렬 : " + sort
        data["title"] = str(i+1)+"순위 필터링"
        data["description"] = description
        data["buttons"] = [
            {
              "action": "message",
              "label": "카린이 시작하기",
              "messageText": "카린이 시작하기"
            },
            {
              "action": "message",
              "label": "카린이 이용방법",
              "messageText": "카린이 이용방법"
            },
            {
              "action": "message",
              "label": "카린이 소개하기",
              "messageText": "카린이 소개하기"
            }
        ]
        list4.append(data)
    

    if filter_rank=="1순위 필터링":
        responseBody = {
          "version": "2.0",
          "template": {
            "outputs": [
      {
        "basicCard": list4[0]
      }
    ]
          }
        }
    elif filter_rank=="2순위 필터링":
        responseBody = {
          "version": "2.0",
          "template": {
            "outputs": [
      {
        "basicCard": list4[1]
      }
    ]
          }
        }
    elif filter_rank=="3순위 필터링":
        responseBody = {
          "version": "2.0",
          "template": {
            "outputs":[
      {
        "basicCard": list4[2]
      }
    ]
          }
        }

    cursor.close()
    conn.commit()
    conn.close()

    return responseBody

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=int(sys.argv[1]), debug=True)