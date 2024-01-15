// pages/api/downloadImage.ts
import type { NextApiRequest, NextApiResponse } from 'next';
import * as https from 'https';
import * as path from 'path';
import * as fs from 'fs';
import sharp from 'sharp';


export default function handler(req: NextApiRequest, res: NextApiResponse) {
  const imageUrl = req.query.imageUrl as string; // 클라이언트로부터 받은 이미지 URL
  const filename = "newImg.png";//path.basename(imageUrl); // URL에서 파일 이름 추출
  const filePath = path.resolve('./public', filename); // 파일을 저장할 경로

  console.log("image url : " + imageUrl);

  const fileStream = fs.createWriteStream(filePath);
  https.get(imageUrl, (response) => {
    response.pipe(fileStream);
    fileStream.on('finish', () => {
      fileStream.close();

      console.log(filePath);

      res.status(200).json({ message: 'Image downloaded and saved successfully.' });
    });
  }).on('error', (error) => {
    res.status(500).json({ message: 'Failed to download image.', error: error.message });
  });
}
