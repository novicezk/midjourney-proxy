"use client";

import React, { useState, useRef, useEffect } from 'react';
import Image from 'next/image';

export default function Home() {
  const [imageUrl, setImageUrl] = useState('');
  const promptInputRef = useRef<HTMLInputElement>(null);

  const handleSubmit = () => {
    if (promptInputRef.current) {
      const prompt = promptInputRef.current.value;
      fetchImage(prompt);
    }
  };

  // 이미지를 서버에서 가져오는 함수
  const fetchImage = async (prompt: string) => {

    try {
      // POST 요청으로 서버에 프롬프트 전송
      const postResponse = await fetch('http://localhost:8080/mj/submit/imagine', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ prompt, base64Array: null, base64: "" })
      });
      const postData = await postResponse.json();
      
      if (postData.code === 1) {

          pollForImage(postData.result);
      }
    } catch (error) {
      console.error('Error fetching image:', error);
    }
  };
  
  // 이미지 경로를 지정하고 함수를 실행합니다.
  const pollForImage = async (resultId : String) => {
    try {
      const interval = setInterval(async () => {
        const response = await fetch(`http://localhost:8080/mj/task/${resultId}/fetch`);
        const data = await response.json();
  
        if (data.imageUrl) {
          if (data.finishTime != null) {
                
            fetch('/api/downloadImage?imageUrl=' + encodeURIComponent(data.imageUrl))
            .then(response => response.json())
            .then(data => console.log(`image _ ${data} download complete`));
            
            setImageUrl(data.imageUrl);

            clearInterval(interval); // 작업 완료 시 인터벌 중지
      
          } else {
            setImageUrl(data.imageUrl);
          }
        } else {
          return;
        }
      }, 1000); // 5초마다 반복
    } catch (error) {
      console.error('Error fetching task:', error);
    }
  };

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <div className="w-full">
        
          <Image
            src={imageUrl}
            alt="Generated Image"
            layout="responsive"
            width={700}
            height={475}
            unoptimized // 외부 이미지를 사용하기 때문에 필요
          />
        
      </div>
      <div>
        <input
          type="text"
          ref={promptInputRef}
          placeholder="Enter a prompt"
          className="border p-2"
          style={{ color: 'black' }}
        />
        <button
          onClick={handleSubmit}
          className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
        >
          Submit
        </button>
      </div>
    </main>
  );
}

